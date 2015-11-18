
package org.ibp.api.security;

import java.nio.charset.Charset;

import org.ibp.Main;
import org.ibp.api.security.xauth.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

	@Configuration
	public static class TestConfiguration {

		@Bean
		// Higher priority to in memory user service for test only.
		@Primary
		public UserDetailsService userDetailsService() {
			// Password is a BCrypt strting for "testuserpassword"
			UserDetails testUser =
					new User("testuser", "$2a$08$UURcoje1Fghoho7ylgCZuuAKPNZOkSTJfen1BJYZ7NxqgGuc1cMv2",
							Lists.newArrayList(new SimpleGrantedAuthority("ADMIN")));
			return new InMemoryUserDetailsManager(Lists.newArrayList(testUser));
		}
	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).apply(SecurityMockMvcConfigurers.springSecurity()).build();
	}

	@Test
	public void testUnauthorizedRequest() throws Exception {
		// Call one of the services requiring authentication (ontology datatype service chosen at random) without auth header.
		// and expect 401 Unauthorized response
		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/datatypes").contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	public void testAuthorizedRequest() throws Exception {

		// Hit the /authentication resource to obtain token first.
		final MvcResult authResult =
				this.mockMvc.perform(MockMvcRequestBuilders.post("/authenticate?username=testuser&password=testuserpassword")).andReturn();

		// Parse the response to get token
		Token token = new ObjectMapper().readValue(authResult.getResponse().getContentAsByteArray(), Token.class);

		// Call one of the services requiring authentication (ontology datatype service chosen at random) with a valid auth header.
		// and expect 200 OK response
		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/datatypes")
				.contentType(this.contentType)
				.header("X-Auth-Token", token.getToken()))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
}
