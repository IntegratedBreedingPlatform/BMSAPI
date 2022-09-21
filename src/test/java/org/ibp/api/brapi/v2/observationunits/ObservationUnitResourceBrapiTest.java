package org.ibp.api.brapi.v2.observationunits;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitPatchRequestDTO;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.domain.search_request.brapi.v2.ObservationUnitsSearchRequestDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.java.dataset.DatasetService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class ObservationUnitResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private ObservationUnitService observationUnitService;

	@Autowired
	private StudyService studyService;

	@Before
	public void setup() {
		Mockito.reset(this.datasetService);
		Mockito.reset(this.searchRequestService);
		Mockito.reset(this.observationUnitService);
		Mockito.reset(this.studyService);

	}

	@Test
	public void testPatchObservationUnit() throws Exception {
		final String observationUnitDbId = RandomStringUtils.randomAlphanumeric(36);

		@SuppressWarnings("unchecked") final Map<String, Object> request = new ObjectMapper().readValue("{"
			+ " \"observationUnitPosition\": {"
			+ "  \"geoCoordinates\": {"
			+ "   \"geometry\": {"
			+ "    \"coordinates\": ["
			+ "     -76.506042,"
			+ "     42.417373"
			+ "    ],"
			+ "    \"type\": \"Point\""
			+ "   },"
			+ "    \"type\": \"Feature\""
			+ "  }"
			+ " }"
			+ "}", HashMap.class);

		this.mockMvc.perform(
			MockMvcRequestBuilders.patch("/{crop}/brapi/v2/observationunits/{observationUnitDbId}", this.cropName, observationUnitDbId)
				.content(this.convertObjectToByte(request))
				.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

		final ArgumentCaptor<String> observationUnitDbIdCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<ObservationUnitPatchRequestDTO> mappedDTOCaptor =
			ArgumentCaptor.forClass(ObservationUnitPatchRequestDTO.class);
		Mockito.verify(this.observationUnitService).update(observationUnitDbIdCaptor.capture(), mappedDTOCaptor.capture());

		assertThat(observationUnitDbIdCaptor.getValue(), is(observationUnitDbId));

		final HashMap<String, Object> geometry =
			(HashMap<String, Object>) mappedDTOCaptor.getValue().getObservationUnitPosition().getGeoCoordinates().get("geometry");
		final List<Double> coordinates = (List<Double>) geometry.get("coordinates");
		assertThat(coordinates.get(0), is(-76.506042));
	}

	@Test
	public void testSearchObservationUnits() throws Exception {

		final ObservationUnitDto observationUnitDto = new ObservationUnitDto();
		observationUnitDto.setProgramDbId("04136e3f-55f9-4a80-9c24-5066a253ce6f");
		observationUnitDto.setTrialDbId("25008");
		observationUnitDto.setTrialName("Trial Name");
		final int searchResultsDbId = nextInt();
		doReturn(new ObservationUnitsSearchRequestDto()).when(this.searchRequestService)
			.getSearchRequest(searchResultsDbId, ObservationUnitsSearchRequestDto.class);
		when(this.observationUnitService
			.searchObservationUnits(Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_SIZE), Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_NUMBER), any(
				ObservationUnitSearchRequestDTO.class))).thenReturn(Arrays.asList(observationUnitDto));
		this.mockMvc.perform(
			MockMvcRequestBuilders.get("/{crop}/brapi/v2/search/observationunits/{searchResultsDbId}", this.cropName, searchResultsDbId)
				.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId", is("04136e3f-55f9-4a80-9c24-5066a253ce6f")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].treatments", empty()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialDbId", is("25008")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialName", is("Trial Name")));
	}

	@Test
	public void testGetObservationUnits() throws Exception {

		final ObservationUnitDto observationUnitDto = new ObservationUnitDto();
		observationUnitDto.setProgramDbId("04136e3f-55f9-4a80-9c24-5066a253ce6f");
		observationUnitDto.setTrialDbId("25008");
		observationUnitDto.setTrialName("Trial Name");

		when(this.observationUnitService
			.searchObservationUnits(Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_SIZE), Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_NUMBER), any(
				ObservationUnitSearchRequestDTO.class))).thenReturn(Arrays.asList(observationUnitDto));

		this.mockMvc.perform(
				MockMvcRequestBuilders.get("/{crop}/brapi/v2/observationunits", this.cropName)
					.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId", is("04136e3f-55f9-4a80-9c24-5066a253ce6f")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].treatments", empty()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialDbId", is("25008")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialName", is("Trial Name")));
	}
}
