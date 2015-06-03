package org.ibp.api.rest.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class StudyResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {
		@Bean
		@Primary
		public org.generationcp.middleware.service.api.study.StudyService getStudyServiceMW() {
			return Mockito.mock(org.generationcp.middleware.service.api.study.StudyService.class);
		}
	}

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

		Mockito.when(this.studyServiceMW.listAllStudies(Mockito.anyString())).thenReturn(summariesMW);
		
		this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/list", "maize")
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(summariesMW.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['id']", Matchers.is(summaryMW.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['name']", Matchers.is(summaryMW.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['title']", Matchers.is(summaryMW.getTitle())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['objective']", Matchers.is(summaryMW.getObjective())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['type']", Matchers.is(summaryMW.getType().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['startDate']", Matchers.is(summaryMW.getStartDate())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['endDate']", Matchers.is(summaryMW.getEndDate())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.studyServiceMW).listAllStudies(Mockito.anyString());
	}
	
	@Test
	public void testGetObservations() throws Exception {
		MeasurementDto measurement = new MeasurementDto(new TraitDto(1, "Plant Height"), 1, "123");
		ObservationDto obsDto = new ObservationDto(1, "1", "Test", 1, "CML123", "1", "CIMMYT Seed Bank", "1", "1", Lists.newArrayList(measurement));
		
		Mockito.when(this.studyServiceMW.getObservations(Mockito.anyInt())).thenReturn(Lists.newArrayList(obsDto));
		this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}/observations", "maize", "1")
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['uniqueIdentifier']", Matchers.is(obsDto.getMeasurementId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['germplasmId']", Matchers.is(obsDto.getGid())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['germplasmDesignation']", Matchers.is(obsDto.getDesignation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['enrtyNumber']", Matchers.is(obsDto.getEntryNo())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['entryType']", Matchers.is(obsDto.getEntryType())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['plotNumber']", Matchers.is(obsDto.getPlotNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['replicationNumber']", Matchers.is(obsDto.getRepitionNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['environmentNumber']", Matchers.is(obsDto.getTrialInstance())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['seedSource']", Matchers.is(obsDto.getSeedSource())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['measurements']", IsCollectionWithSize.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementIdentifier.measurementId", Matchers.is(measurement.getPhenotypeId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementIdentifier.trait.traitId", Matchers.is(measurement.getTrait().getTraitId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementIdentifier.trait.traitName", Matchers.is(measurement.getTrait().getTraitName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementValue", Matchers.is(measurement.getTriatValue())))
				.andDo(MockMvcResultHandlers.print());
	}

}
