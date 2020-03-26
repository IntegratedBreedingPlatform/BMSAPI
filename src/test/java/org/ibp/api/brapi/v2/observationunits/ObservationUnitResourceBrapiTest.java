package org.ibp.api.brapi.v2.observationunits;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitPatchRequestDTO;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.dataset.DatasetService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ObservationUnitResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private ObservationUnitService observationUnitService;

	@Before
	public void setup() {
		Mockito.reset(this.datasetService);
		Mockito.reset(this.searchRequestService);
		Mockito.reset(this.observationUnitService);
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
				.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn();

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
}
