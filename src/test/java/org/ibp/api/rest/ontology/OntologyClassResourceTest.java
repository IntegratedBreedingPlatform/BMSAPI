package org.ibp.api.rest.ontology;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.After;
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

public class OntologyClassResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
		}

		@Bean
		@Primary
		public OntologyManagerService ontologyManagerService() {
			return Mockito.mock(OntologyManagerService.class);
		}
	}

	@Autowired
	private OntologyManagerService ontologyManagerService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;


	@Before
	public void reset() {
		Mockito.reset(this.ontologyManagerService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void listAllClasses() throws Exception {

		List<Term> termList = new ArrayList<>();
		Term term = new Term(1, "Abiotic Stress", "");
		termList.add(term);
		term = new Term(2, "Agronomic", "");
		termList.add(term);
		term = new Term(3, "Biotic Stress", "");
		termList.add(term);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(termList).when(this.ontologyManagerService).getAllTraitClass();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/classes", cropName)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(termList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.is(termList.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService, Mockito.times(1)).getAllTraitClass();
	}
}
