package org.ibp.api.rest.study;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.study.StudySummary;
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

		Mockito.when(this.studyService.listAllStudies()).thenReturn(summaries);

		this.mockMvc
		.perform(
				MockMvcRequestBuilders.get("/study/{cropname}/list", "maize").contentType(
						this.contentType))
						.andExpect(MockMvcResultMatchers.status().isOk())
						.andExpect(
								MockMvcResultMatchers.jsonPath("$",
										IsCollectionWithSize.hasSize(summaries.size())))
										.andExpect(
												MockMvcResultMatchers.jsonPath("$[0]['id']", Matchers.is(summary.getId())))
												.andExpect(
														MockMvcResultMatchers.jsonPath("$[0]['name']",
																Matchers.is(summary.getName())))
																.andExpect(
																		MockMvcResultMatchers.jsonPath("$[0]['title']",
																				Matchers.is(summary.getTitle())))
																				.andExpect(
																						MockMvcResultMatchers.jsonPath("$[0]['objective']",
																								Matchers.is(summary.getObjective())))
																								.andExpect(
																										MockMvcResultMatchers.jsonPath("$[0]['type']",
																												Matchers.is(summary.getType())))
																												.andExpect(
																														MockMvcResultMatchers.jsonPath("$[0]['startDate']",
																																Matchers.is(summary.getStartDate())))
																																.andExpect(
																																		MockMvcResultMatchers.jsonPath("$[0]['endDate']",
																																				Matchers.is(summary.getEndDate())))
																																				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.studyService).listAllStudies();
	}

}
