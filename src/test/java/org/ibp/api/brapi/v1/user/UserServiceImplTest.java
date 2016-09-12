
package org.ibp.api.brapi.v1.user;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class UserServiceImplTest extends ApiUnitTestBase {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Test
	public void testGetAllUsers() throws Exception {
		final UserDto user = new UserDto();
		final String firstName = RandomStringUtils.randomAlphabetic(5);
		user.setFirstName(firstName);
		final String lastName = RandomStringUtils.randomAlphabetic(5);
		user.setLastName(lastName);
		user.setStatus(0);
		user.setRole("Breeder");
		final Integer userId = ThreadLocalRandom.current().nextInt();
		user.setUserId(userId);
		final String username = RandomStringUtils.randomAlphabetic(5);
		user.setUsername(username);

		final List<UserDto> users = Lists.newArrayList(user);

		Mockito.when(this.workbenchDataManager.getAllUserDtosSorted()).thenReturn(users);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/brapi/v1/users").build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(users.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].userId", Matchers.is(user.getUserId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].username", Matchers.is(user.getUsername())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName", Matchers.is(user.getLastName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].role", Matchers.is(user.getRole())));
	}
}
