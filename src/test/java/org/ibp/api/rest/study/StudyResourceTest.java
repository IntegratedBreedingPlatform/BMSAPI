package org.ibp.api.rest.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.study.StudyService;
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

public class StudyResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
		}

		@Bean
		@Primary
		public StudyService getStudyService() {
			return Mockito.mock(StudyService.class);
		}
		
		@Bean
		@Primary
		public org.generationcp.middleware.service.api.study.StudyService getStudyServiceMW() {
			return Mockito.mock(org.generationcp.middleware.service.api.study.StudyService.class);
		}
	}

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private StudyService studyService;
	
	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService studyServiceMW;

	@Test
	public void testListAllStudies() throws Exception {

		List<org.generationcp.middleware.service.api.study.StudySummary> summariesMW = new ArrayList<>();
		org.generationcp.middleware.service.api.study.StudySummary summaryMW = new org.generationcp.middleware.service.api.study.StudySummary();
		summaryMW.setId(1);
		summaryMW.setName("A Maizing Trial");
		summaryMW.setTitle("A Maizing Trial Title");
		summaryMW.setObjective("A Maize the world with new Maize variety.");
		summaryMW.setType(StudyType.T);
		summaryMW.setStartDate("01012015");
		summaryMW.setEndDate("01012015");
		summariesMW.add(summaryMW);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.when(this.studyServiceMW.listAllStudies()).thenReturn(summariesMW);
		
		this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/list", "maize")
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(summariesMW.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['id']", Matchers.is(summaryMW.getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['name']", Matchers.is(summaryMW.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['title']", Matchers.is(summaryMW.getTitle())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['objective']", Matchers.is(summaryMW.getObjective())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['type']", Matchers.is(summaryMW.getType().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['startDate']", Matchers.is(summaryMW.getStartDate())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['endDate']", Matchers.is(summaryMW.getEndDate())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.studyServiceMW).listAllStudies();
	}

}
