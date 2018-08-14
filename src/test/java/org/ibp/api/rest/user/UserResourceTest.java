package org.ibp.api.rest.user;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.user.UserService;
import org.ibp.api.java.impl.middleware.user.UserServiceImpl;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
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

public class UserResourceTest  extends ApiUnitTestBase {

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
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("Required String parameter 'projectUUID' is not present")));
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
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("don't exists users for this projectUUID")));
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
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("An internal error occurred while trying to get the users")));
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
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].role.id", Matchers.is(users.get(0).getRole().getId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].role.description", Matchers.is(users.get(0).getRole().getDescription())));
	}

	/**
	 * Should respond with 200 and List Users. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsers() throws Exception {
		final List<UserDetailDto> users = UserTestDataGenerator.initializeListUserDetailDto();
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/users").build().encode();

		Mockito.when(this.userService.getAllUsersSortedByLastName()).thenReturn(users);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(users.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(users.get(0).getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].username", Matchers.is(users.get(0).getUsername())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName", Matchers.is(users.get(0).getLastName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].role.id", Matchers.is(users.get(0).getRole().getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].role.description", Matchers.is(users.get(0).getRole().getDescription())));
	}

	/**
	 * Should respond with 201 and return the id of the created user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUser() throws Exception {
		final String id = "10";
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();
		final HashMap<String, Object> mapResponse = initializeResponse(id);
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/users").build().encode();

		Mockito.when(this.userService.createUser(Mockito.any(UserDetailDto.class))).thenReturn(mapResponse);

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(id)));
	}

	/**
	 * Should respond with 409 and return the id 0, because happened a error during the creation user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserError() throws Exception {
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();
		final HashMap<String, Object> mapResponse = initializeResponseError("email", "exists");

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/users").build().encode();

		Mockito.when(this.userService.createUser(Mockito.any(UserDetailDto.class))).thenReturn(mapResponse);

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isConflict()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.ERROR.errors.[0].fieldNames.[0]", Matchers.is("email")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.ERROR.errors.[0].message", Matchers.is("exists")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is((String) mapResponse.get("id"))));
	}

	/**
	 * Should respond with 200 and return the id of the update user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUser() throws Exception {
		final String id = "7";
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();
		final HashMap<String, Object> mapResponse = initializeResponse(id);

		Mockito.when(this.userService.updateUser(Mockito.any(UserDetailDto.class))).thenReturn(mapResponse);

		this.mockMvc
				.perform(MockMvcRequestBuilders.put("/users/{id}", id).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(id)));
	}

	/**
	 * Should respond with 404 and return the id 0, because happened a error during the creation user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserError() throws Exception {
		final String id = "7";
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();
		final HashMap<String, Object> mapResponse = initializeResponseError("username", "exists");
		Mockito.when(this.userService.updateUser(Mockito.any(UserDetailDto.class))).thenReturn(mapResponse);

		this.mockMvc
				.perform(MockMvcRequestBuilders.put("/users/{id}", id).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isNotFound()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.ERROR.errors.[0].fieldNames.[0]", Matchers.is("username")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.ERROR.errors.[0].message", Matchers.is("exists")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("0")));
	}

	/**
	 * initialize Response error
	 *
	 * @return HashMap<String, Object>
	 */
	public HashMap<String, Object> initializeResponseError(final String fieldname, final String message) {
		final HashMap<String, Object> mapResponse = new HashMap<String, Object>();
		final ErrorResponse errResponse = new ErrorResponse();

		errResponse.addError(message, fieldname);
		mapResponse.put("ERROR", errResponse);
		mapResponse.put("id", "0");
		return mapResponse;
	}

	/**
	 * initialize Response
	 *
	 * @return HashMap<String, Object>
	 */
	public HashMap<String, Object> initializeResponse(final String id) {
		final HashMap<String, Object> mapResponse = new HashMap<String, Object>();

		mapResponse.put("id", id);
		return mapResponse;
	}

}
