package org.ibp.api.rest.user;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.user.UserService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

public class UserResourceTest  extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean(name = "userServiceAPI")
		@Primary
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}
	}

	@Resource(name = "userServiceAPI")
	private UserService userService;

	/**
	 * Should respond with 500 and error message (don't exists users for this projectUUID). * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailDoNotHaveUsers() throws Exception {
		Mockito.when(this.userService.getUsersByProjectUUID(this.programUuid))
				.thenThrow(new IllegalStateException("don't exists users for this projectUUID"));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/users/filter?cropName=" + this.cropName + "&programUUID=" + this.programUuid).contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("don't exists users for this projectUUID")));
	}

	/**
	 * Should respond with 500 and error message (An internal error occurred while trying to get the users). * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuidFailQuery() throws Exception {
		Mockito.when(this.userService.getUsersByProjectUUID(this.programUuid))
				.thenThrow(new NullPointerException("An internal error occurred while trying to get the users"));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/users/filter?cropName=" + this.cropName + "&programUUID=" + this.programUuid).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isInternalServerError()).andExpect(
				MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("An internal error occurred while trying to get the users")));
	}

	/**
	 * Should respond with 200 and List Users. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testListUsersByProjectUuid() throws Exception {
		final List<UserDetailDto> users = UserTestDataGenerator.initializeListUserDetailDto();
		Mockito.when(this.userService.getUsersByProjectUUID(this.programUuid)).thenReturn(users);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/users/filter?cropName=" + this.cropName + "&programUUID=" + this.programUuid).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(users.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(users.get(0).getId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].username", Matchers.is(users.get(0).getUsername())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName", Matchers.is(users.get(0).getLastName())))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$[0].userRoles[0].role.id", Matchers.is(users.get(0).getUserRoles().get(0).getRole().getId())));
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
			.andExpect(
				MockMvcResultMatchers.jsonPath("$[0].userRoles[0].role.id", Matchers.is(users.get(0).getUserRoles().get(0).getRole().getId())));
	}

	/**
	 * Should respond with 201 and return the id of the created user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUser() throws Exception {
		final Integer id = 10;
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailWithAdminRoleDto();
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/users").build().encode();

		Mockito.when(this.userService.createUser(Mockito.any(UserDetailDto.class))).thenReturn(id);

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.is(id.toString())));
	}

	/**
	 * Should respond with 409 and return the id 0, because happened a error during the creation user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserError() throws Exception {
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailWithAdminRoleDto();
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/users").build().encode();

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), UserValidator.class.getName());
		errors.reject(UserValidator.SIGNUP_FIELD_INVALID_EMAIL_FORMAT, "");
		Mockito.doThrow(new ApiRequestValidationException(errors.getAllErrors())).when(this.userService)
			.createUser(Mockito.any(UserDetailDto.class));

		this.mockMvc
			.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
				.content(this.convertObjectToByte(user)))
			.andExpect(MockMvcResultMatchers.status().isBadRequest()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors.[0].message",
				Matchers.is("Email is invalid.")));
	}

	/**
	 * Should respond with 200 and return the id of the update user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUser() throws Exception {
		final Integer id = 7;
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailWithAdminRoleDto();

		Mockito.when(this.userService.updateUser(Mockito.any(UserDetailDto.class))).thenReturn(id);

		this.mockMvc
				.perform(MockMvcRequestBuilders.put("/users/{id}", id).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.is(id.toString())));
	}

	/**
	 * Should respond with 404 and return the id 0, because happened a error during updating the user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserError() throws Exception {
		final Integer id = 7;
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailWithAdminRoleDto();

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), UserValidator.class.getName());
		errors.reject(UserValidator.SIGNUP_FIELD_INVALID_EMAIL_FORMAT, "");
		Mockito.doThrow(new ApiRequestValidationException(errors.getAllErrors())).when(this.userService)
			.updateUser(Mockito.any(UserDetailDto.class));
		this.mockMvc
				.perform(MockMvcRequestBuilders.put("/users/{id}", id).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
			.andExpect(MockMvcResultMatchers.status().isBadRequest()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors.[0].message",
				Matchers.is("Email is invalid.")));
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
