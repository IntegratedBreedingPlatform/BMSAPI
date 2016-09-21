
package org.ibp.api.brapi.v1.user;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.common.GenericResponse;
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

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class UserResourceBrapiTest extends ApiUnitTestBase {

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
	public void testListUsers() throws Exception {
		final List<UserDetailDto> users = responseListUser();
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/brapi/v1/users").build().encode();

		Mockito.when(this.userService.getAllUserDtosSorted()).thenReturn(users);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(users.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(users.get(0).getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].username", Matchers.is(users.get(0).getUsername())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName", Matchers.is(users.get(0).getLastName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].role", Matchers.is(users.get(0).getRole())));
	}

	/**
	 * Should respond with 201 and return the id of the created user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUser() throws Exception {
		final UserDetailDto user = inicializeUser();
		final GenericResponse response = new GenericResponse("10");
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/brapi/v1/users").build().encode();

		Mockito.when(this.userService.createUser(Mockito.any(org.ibp.api.brapi.v1.user.UserDetailDto.class))).thenReturn(response);

		this.mockMvc.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isCreated()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(response.getId())));
	}

	/**
	 * Should respond with 409 and return the id 0, 
	 * because happened a error during the creation user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserError() throws Exception {
		final UserDetailDto user = inicializeUser();
		final GenericResponse response = new GenericResponse("0");
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/brapi/v1/users").build().encode();

		Mockito.when(this.userService.createUser(Mockito.any(org.ibp.api.brapi.v1.user.UserDetailDto.class))).thenReturn(response);

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
						.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isConflict()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(response.getId())));
	}

	/**
	 * Should respond with 200 and return the id of the update user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUser() throws Exception {
		final String id = "7";
		final UserDetailDto user = inicializeUser();
		final GenericResponse response = new GenericResponse("7");
		
		Mockito.when(this.userService.updateUser(Mockito.any(org.ibp.api.brapi.v1.user.UserDetailDto.class))).thenReturn(response);
		
		this.mockMvc.perform(MockMvcRequestBuilders.put("/brapi/v1/users/{id}", id).contentType(this.contentType)
				.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("7")));
	}

	/**
	 * Should respond with 404 and return the id 0, 
	 * because happened a error during the creation user. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserError() throws Exception {
		final String id = "7";
		final UserDetailDto user = inicializeUser();
		final GenericResponse response = new GenericResponse("0");

		Mockito.when(this.userService.updateUser(Mockito.any(org.ibp.api.brapi.v1.user.UserDetailDto.class))).thenReturn(response);

		this.mockMvc.perform(MockMvcRequestBuilders.put("/brapi/v1/users/{id}", id).contentType(this.contentType)
				.content(this.convertObjectToByte(user)))
				.andExpect(MockMvcResultMatchers.status().isNotFound()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("0")));
	}

	/**
	 * Inicialize UserDetailDto
	 * @return UserDetailDto 
	 */
	public UserDetailDto inicializeUser() {
		UserDetailDto user = new UserDetailDto();
		final String firstName = RandomStringUtils.randomAlphabetic(5);
		user.setFirstName(firstName);
		final String lastName = RandomStringUtils.randomAlphabetic(5);
		user.setLastName(lastName);
		user.setStatus("true");
		user.setRole("Breeder");
		final Integer userId = ThreadLocalRandom.current().nextInt();
		user.setId(userId);
		final String username = RandomStringUtils.randomAlphabetic(5);
		user.setUsername(username);
		return user;
	}

	/**
	 * Inicialize List UserDetailDto
	 * @return List<UserDetailDto> 
	 */
	public List<UserDetailDto>  responseListUser() {
		UserDetailDto user = new UserDetailDto();
		final String firstName = RandomStringUtils.randomAlphabetic(5);
		user.setFirstName(firstName);
		final String lastName = RandomStringUtils.randomAlphabetic(5);
		user.setLastName(lastName);
		user.setStatus("true");
		user.setRole("Breeder");
		final Integer userId = ThreadLocalRandom.current().nextInt();
		user.setId(userId);
		final String username = RandomStringUtils.randomAlphabetic(5);
		user.setUsername(username);
		
		final List<UserDetailDto> users = Lists.newArrayList(user);
		return users;
	}

}
