
package org.ibp.api.rest.ontology;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.ontology.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

import java.util.ArrayList;
import java.util.List;

public class ClassResourceTest extends ApiUnitTestBase {

	@Autowired
	protected ModelService modelService;

	@Autowired
	private TermDataManager termDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.modelService);
		Mockito.reset(this.termDataManager);
	}

	@Test
	public void listAllClasses() throws Exception {

		List<String> classes = new ArrayList<>();
		classes.add(TestDataProvider.MW_TERM_LIST.get(0).getName());
		classes.add(TestDataProvider.MW_TERM_LIST.get(1).getName());
		classes.add(TestDataProvider.MW_TERM_LIST.get(2).getName());

		Mockito.doReturn(TestDataProvider.MW_TERM_LIST).when(this.termDataManager).getTermByCvId(CvId.TRAIT_CLASS.getId());
		Mockito.doReturn(classes).when(this.modelService).getAllClasses();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{cropname}/classes?programUUID=" + this.programUuid, this.cropName).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(TestDataProvider.MW_TERM_LIST.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.is(TestDataProvider.MW_TERM_LIST.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());
	}
}
