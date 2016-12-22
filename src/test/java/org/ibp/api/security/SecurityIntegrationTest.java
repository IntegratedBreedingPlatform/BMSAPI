
package org.ibp.api.security;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.net.URL;
import java.nio.charset.Charset;

import org.ibp.Main;
import org.ibp.api.brapi.v1.security.auth.TokenRequest;
import org.ibp.api.security.xauth.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = Main.class)
public class SecurityIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(),
			Charset.forName("utf8"));

	private static final String TEST_USER = "testusername";
	private static final String TEST_PASS = "testuserpassword";
	private static final String TEST_ROLE = "ADMIN";

	@Configuration
	public static class TestConfiguration {

		@Bean
		// Higher priority to in memory user service for test only.
		@Primary
		public UserDetailsService userDetailsService() {
			// Application context is using BCryptPasswordEncoder so setting encrypted password in memory test user store.
			String bcryptPassword = new BCryptPasswordEncoder().encode(TEST_PASS);
			UserDetails testUser = new User(TEST_USER, bcryptPassword, Lists.newArrayList(new SimpleGrantedAuthority(TEST_ROLE)));
			return new InMemoryUserDetailsManager(Lists.newArrayList(testUser));
		}
	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).apply(SecurityMockMvcConfigurers.springSecurity()).build();
	}

	@Test
	public void testUnauthorizedRequest() throws Exception {
		// Call one of the services requiring authentication (ontology datatype service chosen at random) without auth header
		// and expect 401 Unauthorized response
		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/datatypes").contentType(this.contentType))
		.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}
	
	@Test
	public void testOptionsRequestsAlwaysPermitted() throws Exception {
		// Make an OPTIONS request (crop listing service chosen at random). It should always be allowed without authentication token.
		this.mockMvc
				.perform(MockMvcRequestBuilders.options("/crop/list") //
				.contentType(this.contentType)) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testAuthorizedRequest() throws Exception {

		// Hit the /authentication resource to obtain token first.
		String tokenUrl = String.format("/authenticate?username=%s&password=%s", TEST_USER, TEST_PASS);
		final MvcResult authResult = this.mockMvc.perform(MockMvcRequestBuilders.post(tokenUrl)).andReturn();
		Assert.assertEquals("Was expecting a successful token retrieval.", HttpStatus.OK.value(), authResult.getResponse().getStatus());

		// Parse the response to get token
		Token token = new ObjectMapper().readValue(authResult.getResponse().getContentAsByteArray(), Token.class);

		// Call one of the services requiring authentication (ontology datatype service chosen at random) with a valid auth header
		//   and expect 200 OK response
		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/datatypes")
				.contentType(this.contentType)
				.header("X-Auth-Token", token.getToken()))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testAuthenticateBrapi() throws Exception {
		final TokenRequest tokenRequest = new TokenRequest();
		tokenRequest.setUsername(TEST_USER);
		tokenRequest.setPassword(TEST_PASS);

		final String body = "{\n" +
				"    \"grant_type\" : \"password\",\n" +
				"    \"username\" : \""+ TEST_USER +"\",\n" +
				"    \"password\" : \""+ TEST_PASS +"\",\n" +
				"    \"client_id\" : \"\"\n" +
				"}\n" +
				"";

		this.mockMvc.perform(MockMvcRequestBuilders.post("/brapi/v1/token").content(body.getBytes()).contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(jsonPath("$.userDisplayName", is(TEST_USER)))
				.andExpect(jsonPath("$.access_token", not(isEmptyOrNullString())))
				.andExpect(jsonPath("$.metadata.datafiles", emptyCollectionOf(URL.class)))
				.andExpect(jsonPath("$.metadata.pagination", nullValue()))
				.andExpect(jsonPath("$.metadata.status", nullValue()))
				.andExpect(jsonPath("$.expires_in", not(isEmptyOrNullString())));
	}
}
