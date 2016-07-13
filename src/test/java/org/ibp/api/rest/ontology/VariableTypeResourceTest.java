
package org.ibp.api.rest.ontology;

import java.util.List;

import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.data.initializer.TestDataProvider;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.java.ontology.ModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class VariableTypeResourceTest extends ApiUnitTestBase {

	@Autowired
	protected ModelService modelService;

	@Before
	public void reset() {
		Mockito.reset(this.modelService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void listAllVariableTypes() throws Exception {

		List<VariableType> variableTypes = TestDataProvider.getVariableTypes();

		Mockito.doReturn(variableTypes).when(this.modelService).getAllVariableTypes();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/variableTypes").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(variableTypes.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(variableTypes.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());

	}
}
