
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

public class SampleResourceBrapiTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public SampleService sampleService() {
			return Mockito.mock(SampleService.class);
		}
	}


	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

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

		final SampleDetailsDTO sampleDetailsDTO = SampleTestDataGenerator.createRandomSampleDetails();
		Mockito.when(this.sampleService.getSampleObservation(sampleId)).thenReturn(sampleDetailsDTO);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.studyDbId", Matchers.is(sampleDetailsDTO.getStudyDbId().toString()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.observationUnitDbId", Matchers.is(sampleDetailsDTO.getObsUnitId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.sampleDbId", Matchers.is(sampleDetailsDTO.getSampleBusinessKey()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.takenBy", Matchers.is(sampleDetailsDTO.getTakenBy()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.sampleType", Matchers.is(sampleDetailsDTO.getSampleType()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.tissueType", Matchers.is(sampleDetailsDTO.getTissueType()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.notes", Matchers.is(sampleDetailsDTO.getNotes()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.germplasmDbId", Matchers.is(sampleDetailsDTO.getGermplasmUUID()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.plateDbId", Matchers.is(sampleDetailsDTO.getPlateId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.plateIndex", Matchers.is(sampleDetailsDTO.getSampleNumber()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.plotDbId", Matchers.is(sampleDetailsDTO.getPlotNo().toString()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.sampleTimestamp",
				Matchers.is(SampleResourceBrapiTest.DATE_FORMAT.format(sampleDetailsDTO.getSampleDate())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))) //
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

		final SampleDetailsDTO sampleDetailsDTO = new SampleDetailsDTO();
		Mockito.when(this.sampleService.getSampleObservation(sampleId)).thenReturn(sampleDetailsDTO);

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination", Matchers.isEmptyOrNullString())) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0].message", Matchers.is("not found sample"))) //
		;
	}
}
