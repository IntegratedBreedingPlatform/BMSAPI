
package org.ibp.api.brapi.v1.role;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class RoleServiceImplTest extends ApiUnitTestBase {

	@Test
	public void testGetAllUsers() throws Exception {
		List<RoleDto> roles = new ArrayList<RoleDto>();

		RoleDto admin = new RoleDto(1, "ADMIN");
		RoleDto breeder = new RoleDto(2, "BREEDER");
		RoleDto technician = new RoleDto(3, "TECHNICIAN");

		roles.add(admin);
		roles.add(breeder);
		roles.add(technician);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/brapi/v1/roles").build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(roles.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(admin.getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(admin.getDescription())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(breeder.getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].description", Matchers.is(breeder.getDescription())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[2].id", Matchers.is(technician.getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[2].description", Matchers.is(technician.getDescription())));
	}
}
