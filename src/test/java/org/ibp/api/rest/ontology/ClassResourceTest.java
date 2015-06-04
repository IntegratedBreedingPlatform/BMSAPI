
package org.ibp.api.rest.ontology;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class ClassResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public TermDataManager termDataManager() {
			return Mockito.mock(TermDataManager.class);
		}
	}

	@Autowired
	private TermDataManager termDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.termDataManager);
	}

	@Test
	public void listAllClasses() throws Exception {
		Mockito.doReturn(TestDataProvider.mwTermList).when(this.termDataManager).getTermByCvId(CvId.TRAIT_CLASS.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/classes", this.cropName).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(TestDataProvider.mwTermList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.is(TestDataProvider.mwTermList.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.termDataManager, Mockito.times(1)).getTermByCvId(CvId.TRAIT_CLASS.getId());
	}
}
