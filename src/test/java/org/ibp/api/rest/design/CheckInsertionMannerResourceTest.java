package org.ibp.api.rest.design;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class CheckInsertionMannerResourceTest extends ApiUnitTestBase {

	@Test
	public void testRetrieveCheckInsertionManners() throws Exception {

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/check-insertion-manners", "maize")
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(2)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is("8414")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("1")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is("Insert each check in turn")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is("8415")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is("2")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].description", Matchers.is("Insert all checks at each position")));
	}

}
