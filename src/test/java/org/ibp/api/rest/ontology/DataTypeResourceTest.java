
package org.ibp.api.rest.ontology;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.DataType;
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

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class DataTypeResourceTest extends ApiUnitTestBase {

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
	public void listAllDataTypes() throws Exception {

		List<DataType> dataTypes = new ArrayList<>();
		DataType dataType = TestDataProvider.NUMERICAL_DATA_TYPE;
		dataTypes.add(dataType);
		dataType = TestDataProvider.CATEGORICAL_DATA_TYPE;
		dataTypes.add(dataType);

		Mockito.doReturn(dataTypes).when(this.modelService).getAllDataTypes();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/datatypes").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(dataTypes.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(dataTypes.get(0).getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(dataTypes.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());
	}
}
