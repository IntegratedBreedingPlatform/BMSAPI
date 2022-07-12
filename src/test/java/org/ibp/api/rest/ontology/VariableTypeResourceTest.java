
package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.ontology.ModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

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

		final List<VariableType> variableTypes = TestDataProvider.getVariableTypes();
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/crops/wheat/variable-types")
			.queryParam("programUUID", this.programUuid)
			.queryParam("excludeRestrictedTypes", false).build().encode();
		Mockito.doReturn(variableTypes).when(this.modelService).getAllVariableTypes(false);
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(variableTypes.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(variableTypes.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());

	}
}
