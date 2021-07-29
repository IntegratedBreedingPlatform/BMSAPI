
package org.ibp.api.brapi.v1.sample;

import com.beust.jcommander.internal.Lists;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
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
import java.util.ArrayList;
import java.util.Date;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

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

		final SampleObservationDto sampleObservationDto = this.createRandomSampleObservationDto();
		final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO();
		requestDTO.setSampleDbId(sampleId);
		Mockito.when(this.sampleService.getSampleObservations(requestDTO, null)).thenReturn(Lists.newArrayList(sampleObservationDto));

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.studyDbId", Matchers.is(sampleObservationDto.getStudyDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.observationUnitDbId", Matchers.is(sampleObservationDto.getObservationUnitDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.sampleDbId", Matchers.is(sampleObservationDto.getSampleDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.takenBy", Matchers.is(sampleObservationDto.getTakenBy()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.sampleType", Matchers.is(sampleObservationDto.getSampleType()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.tissueType", Matchers.is(sampleObservationDto.getTissueType()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.notes", Matchers.is(sampleObservationDto.getNotes()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.germplasmDbId", Matchers.is(sampleObservationDto.getGermplasmDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.plateDbId", Matchers.is(sampleObservationDto.getPlateDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.plateIndex", Matchers.is(sampleObservationDto.getPlateIndex()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.plotDbId", Matchers.is(sampleObservationDto.getPlotDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.plantDbId", Matchers.is(sampleObservationDto.getPlantDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.sampleTimestamp",
				Matchers.is(SampleResourceBrapiTest.DATE_FORMAT.format(sampleObservationDto.getSampleTimestamp())))); //*/

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

		final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO();
		requestDTO.setSampleDbId(sampleId);
		Mockito.when(this.sampleService.getSampleObservations(requestDTO, null)).thenReturn(new ArrayList<>());

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0].message", Matchers.is("not found sample"))) //
		;
	}

	private SampleObservationDto createRandomSampleObservationDto() {
		final SampleObservationDto sampleObservationDto =
				new SampleObservationDto();
		sampleObservationDto.setGermplasmDbId(randomAlphanumeric(10));
		sampleObservationDto.setNotes(randomAlphanumeric(6));
		sampleObservationDto.setObservationUnitDbId(randomAlphanumeric(10));
		sampleObservationDto.setPlantDbId(randomAlphanumeric(5));
		sampleObservationDto.setPlateDbId(randomAlphanumeric(10));
		sampleObservationDto.setPlateIndex(Integer.valueOf(randomNumeric(2)));
		sampleObservationDto.setPlotDbId(randomAlphanumeric(2));
		sampleObservationDto.setSampleDbId(randomAlphanumeric(10));
		sampleObservationDto.setSampleTimestamp(new Date());
		sampleObservationDto.setTakenBy(randomAlphanumeric(6));
		sampleObservationDto.setStudyDbId(randomNumeric(2));
		sampleObservationDto.setSampleType(randomAlphanumeric(6));
		sampleObservationDto.setTissueType(randomAlphanumeric(6));


		return sampleObservationDto;
	}
}
