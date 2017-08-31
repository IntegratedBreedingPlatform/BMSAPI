package org.ibp.api.rest.user;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.user.UserDetailDto;
import org.ibp.api.java.user.UserService;
import org.ibp.api.java.impl.middleware.user.UserServiceImpl;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@ActiveProfiles("UserResource-mocked")
public class UserResourceTest  extends ApiUnitTestBase {

	@Profile("UserResource-mocked")
	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public UserService userService() {
			return Mockito.mock(UserServiceImpl.class);
		}
	}

	@Autowired
	private UserService userService;

	/**
	 * Should respond with 500 and error message (Required String parameter 'projectUUID' is not present). * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailMissParameter() throws Exception {
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isInternalServerError())
			.andExpect(MockMvcResultMatchers.jsonPath("errors.$[0].message", Matchers.is("Required String parameter 'projectUUID' is not present")));
	}

	/**
	 * Should respond with 500 and error message (don't exists users for this projectUUID). * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailDoNotHaveUsers() throws Exception {
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").queryParam("projectUUID", "d8d59d89-f4ca-4b83-90e2-be2d82407144").build().encode();
		Mockito.when(this.userService.getUsersByProjectUUID("d8d59d89-f4ca-4b83-90e2-be2d82407144")).thenThrow(new IllegalStateException("don't exists users for this projectUUID"));

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isInternalServerError())
			.andExpect(MockMvcResultMatchers.jsonPath("errors.$[0].message", Matchers.is("don't exists users for this projectUUID")));
	}

	/**
	 * Should respond with 500 and error message (An internal error occurred while trying to get the users). * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailQuery() throws Exception {
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").queryParam("projectUUID", "d8d59d89-f4ca-4b83-90e2-be2d82407145").build().encode();
		Mockito.when(this.userService.getUsersByProjectUUID("d8d59d89-f4ca-4b83-90e2-be2d82407145")).thenThrow( new NullPointerException("An internal error occurred while trying to get the users"));

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isInternalServerError())
			.andExpect(MockMvcResultMatchers.jsonPath("errors.$[0].message", Matchers.is("An internal error occurred while trying to get the users")));
	}

	/**
	 * Should respond with 200 and List Users. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuid() throws Exception {
		final List<UserDetailDto> users = UserTestDataGenerator.initializeListUserDetailDto();
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/user/list").queryParam("projectUUID", "d8d59d89-f4ca-4b83-90e2-be2d82407146").build().encode();

		Mockito.when(this.userService.getUsersByProjectUUID("d8d59d89-f4ca-4b83-90e2-be2d82407146")).thenReturn(users);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(users.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(users.get(0).getId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].username", Matchers.is(users.get(0).getUsername())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName", Matchers.is(users.get(0).getLastName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].role", Matchers.is(users.get(0).getRole())));
	}



}
