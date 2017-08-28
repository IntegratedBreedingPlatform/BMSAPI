package org.ibp.api.rest.user;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.user.UserDetailDto;
import org.ibp.api.brapi.v1.user.UserService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class UserResourceTest  extends ApiUnitTestBase {
	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}
	}

	@Autowired
	private UserService userService;



	/**
	 * Should respond with 200 and List Users. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuid() throws Exception {
		final List<UserDetailDto> users = responseListUser();
		final Map<String,Object> usersMap = new HashMap<>();
		usersMap.put("USERS", users);
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").queryParam("projectUUID", "d8d59d89-f4ca-4b83-90e2-be2d82407146").build().encode();

		Mockito.when(this.userService.getUsersByProjectUUID(Mockito.anyString())).thenReturn(usersMap);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("USERS.$", IsCollectionWithSize.hasSize(users.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("USERS.$[0].id", Matchers.is(users.get(0).getId())))
			.andExpect(MockMvcResultMatchers.jsonPath("USERS.$[0].username", Matchers.is(users.get(0).getUsername())))
			.andExpect(MockMvcResultMatchers.jsonPath("USERS.$[0].lastName", Matchers.is(users.get(0).getLastName())))
			.andExpect(MockMvcResultMatchers.jsonPath("USERS.$[0].role", Matchers.is(users.get(0).getRole())));
	}


	/**
	 * Should respond with 500 and List Users. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailMissParameter() throws Exception {
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isInternalServerError()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("errors.$[0].message", Matchers.is("Required String parameter 'projectUUID' is not present")));
	}

	/**
	 * Should respond with 404 and List Users. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailDoNotHaveUsers() throws Exception {
		final Map<String,Object> usersMap = new HashMap<>();
		usersMap.put("ERROR", "don't exists users for this projectUUID");
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").queryParam("projectUUID", "d8d59d89-f4ca-4b83-90e2-be2d82407146").build().encode();

		Mockito.when(this.userService.getUsersByProjectUUID(Mockito.anyString())).thenReturn(usersMap);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isNotFound()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("ERROR", Matchers.is("don't exists users for this projectUUID")));
	}

	/**
	 * Should respond with 404 and List Users. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailQuery() throws Exception {
		final Map<String,Object> usersMap = new HashMap<>();
		usersMap.put("ERROR", "An internal error occurred while trying to get the users");
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").queryParam("projectUUID", "d8d59d89-f4ca-4b83-90e2-be2d82407146").build().encode();

		Mockito.when(this.userService.getUsersByProjectUUID(Mockito.anyString())).thenReturn(usersMap);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("ERROR", Matchers.is("An internal error occurred while trying to get the users")));
	}

	/**
	 * initialize List UserDetailDto
	 *
	 * @return List<UserDetailDto>
	 */
	public List<UserDetailDto> responseListUser() {
		final UserDetailDto user = new UserDetailDto();
		final String firstName = RandomStringUtils.randomAlphabetic(5);
		final String lastName = RandomStringUtils.randomAlphabetic(5);
		final Integer userId = ThreadLocalRandom.current().nextInt();
		final String username = RandomStringUtils.randomAlphabetic(5);

		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setStatus("true");
		user.setRole("Breeder");
		user.setId(userId);
		user.setUsername(username);

		final List<UserDetailDto> users = Lists.newArrayList(user);
		return users;
	}

}
