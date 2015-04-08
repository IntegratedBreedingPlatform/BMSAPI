package org.ibp.api.rest.ontology;

import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class OntologyVariableTypeResourceTest extends ApiUnitTestBase {

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void listAllVariableTypes() throws Exception {

		String cropName = "maize";

		this.mockMvc
		.perform(
				MockMvcRequestBuilders.get("/ontology/{cropname}/variableTypes", cropName)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(9)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("Analysis")))
				.andDo(MockMvcResultHandlers.print());

	}
}
