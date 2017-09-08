package org.ibp.api.brapi.v1.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.SampleTestDataGenerator;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class SampleResourceBrapiTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public SampleService sampleService() {
			return  Mockito.mock(SampleService.class);
		}
	}

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);


	@Autowired
	private SampleService sampleService;

	/**
	 * Should respond with 200 and a Sample. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testFoundASample() throws Exception {
		final String sampleId = "a1w2xed2f";
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/samples/" + sampleId).build().encode();

		SampleDetailsDTO sampleDetailsDTO = SampleTestDataGenerator.createRandomSampleDetails();
		Mockito.when(this.sampleService.getSampleObservation(sampleId)).thenReturn(sampleDetailsDTO);

		this.mockMvc.perform(
			MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$.result.studyDbId", Matchers.is(sampleDetailsDTO.getStudyDbId()))) //
			.andExpect(jsonPath("$.result.locationDbId", Matchers.is(sampleDetailsDTO.getLocationDbId()))) //
			.andExpect(jsonPath("$.result.plotId", Matchers.is(sampleDetailsDTO.getPlotId()))) //
			.andExpect(jsonPath("$.result.plantId", Matchers.is(sampleDetailsDTO.getPlantBusinessKey()))) //
			.andExpect(jsonPath("$.result.sampleId", Matchers.is(sampleDetailsDTO.getSampleBusinessKey()))) //
			.andExpect(jsonPath("$.result.takenBy", Matchers.is(sampleDetailsDTO.getTakenBy()))) //
			.andExpect(jsonPath("$.result.sampleDate", Matchers.is(DATE_FORMAT.format(sampleDetailsDTO.getSampleDate())))) //
			.andExpect(jsonPath("$.result.sampleType", Matchers.is(sampleDetailsDTO.getSampleType()))) //
			.andExpect(jsonPath("$.result.tissueType", Matchers.is(sampleDetailsDTO.getTissueType()))) //
			.andExpect(jsonPath("$.result.notes", Matchers.is(sampleDetailsDTO.getNotes()))) //
			.andExpect(jsonPath("$.result.studyName", Matchers.is(sampleDetailsDTO.getStudyName()))) //
			.andExpect(jsonPath("$.result.season", Matchers.is(sampleDetailsDTO.getSeason()))) //
			.andExpect(jsonPath("$.result.locationName", Matchers.is(sampleDetailsDTO.getLocationName()))) //
			.andExpect(jsonPath("$.result.entryNumber", Matchers.is(sampleDetailsDTO.getEntryNo()))) //
			.andExpect(jsonPath("$.result.plotNumber", Matchers.is(sampleDetailsDTO.getPlotNo()))) //
			.andExpect(jsonPath("$.result.germplasmDbId", Matchers.is(sampleDetailsDTO.getGid()))) //
			.andExpect(jsonPath("$.result.plantingDate", Matchers.is(sampleDetailsDTO.getSeedingDate()))) //
			.andExpect(jsonPath("$.result.harvestDate", Matchers.is(sampleDetailsDTO.getHarvestDate()))) //

			.andExpect(jsonPath("$.metadata.pagination.pageNumber", Matchers.is(1))) //
			.andExpect(jsonPath("$.metadata.pagination.pageSize", Matchers.is(1))) //
			.andExpect(jsonPath("$.metadata.pagination.totalCount", Matchers.is(1))) //
			.andExpect(jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))) //
			;
	}

	/**
	 * Should respond with 404. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void testNotFoundASample() throws Exception {
		final String sampleId = "a1w2xed2f";
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/samples/" + sampleId).build().encode();

		SampleDetailsDTO sampleDetailsDTO = new SampleDetailsDTO();
		Mockito.when(this.sampleService.getSampleObservation(sampleId)).thenReturn(sampleDetailsDTO);

		this.mockMvc.perform(
			MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(jsonPath("$.metadata.pagination", Matchers.isEmptyOrNullString())) //
			.andExpect(jsonPath("$.metadata.status.message", Matchers.is("not found sample"))) //
		;
	}
}
