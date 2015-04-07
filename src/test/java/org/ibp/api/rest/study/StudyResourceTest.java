package org.ibp.api.rest.study;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.bms.ApiUnitTestBase;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.java.study.StudyService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

public class StudyResourceTest extends ApiUnitTestBase {
	
	@Configuration
    public static class TestConfiguration {
        @Bean
        @Primary
        public StudyService getStudyService() {
            return Mockito.mock(StudyService.class);
        }
    }
	
	@Autowired
	private StudyService studyService;
	
	@Test
	public void testListAllStudies() throws Exception {
		
		List<StudySummary> summaries = new ArrayList<>();
		StudySummary summary = new StudySummary();
		summary.setId(1);
		summary.setName("A Maizing Trial");
		summary.setTitle("A Maizing Trial Title");
		summary.setObjective("A Maize the world with new Maize variety.");
		summary.setType("TRIAL");
		summary.setStartDate("01012015");
		summary.setEndDate("01012015");
		summaries.add(summary);
		
		Mockito.when(studyService.listAllStudies()).thenReturn(summaries);
		
		mockMvc.perform(get("/study/{cropname}/list", "maize").contentType(contentType))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", hasSize(summaries.size())))
			   .andExpect(jsonPath("$[0]['id']", is(summary.getId())))
			   .andExpect(jsonPath("$[0]['name']", is(summary.getName())))
			   .andExpect(jsonPath("$[0]['title']", is(summary.getTitle())))
			   .andExpect(jsonPath("$[0]['objective']", is(summary.getObjective())))
			   .andExpect(jsonPath("$[0]['type']", is(summary.getType())))
			   .andExpect(jsonPath("$[0]['startDate']", is(summary.getStartDate())))
			   .andExpect(jsonPath("$[0]['endDate']", is(summary.getEndDate())))
			   .andDo(print());
		
		Mockito.verify(studyService).listAllStudies();
	}

}
