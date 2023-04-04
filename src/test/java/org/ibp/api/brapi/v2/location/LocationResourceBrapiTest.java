
package org.ibp.api.brapi.v2.location;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.api.location.Coordinate;
import org.generationcp.middleware.api.location.Geometry;
import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.doReturn;

public class LocationResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private LocationService locationService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Test
	public void testPostSearchLocations() throws Exception {
		final Integer locationDbId = RandomUtils.nextInt();
		final Integer searchResultsDbId = 1;
		final LocationSearchRequest requestDTO = new LocationSearchRequest();
		requestDTO.setLocationDbIds(Collections.singletonList(locationDbId));

		Mockito.doReturn(searchResultsDbId).when(this.searchRequestService).saveSearchRequest(requestDTO, LocationSearchRequest.class);

		this.mockMvc.perform(MockMvcRequestBuilders.post("/maize/brapi/v2/search/locations")
				.content(this.convertObjectToByte(requestDTO))
				.contentType(this.contentType)
				.locale(Locale.getDefault()))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.searchResultsDbId", Matchers.is(String.valueOf(searchResultsDbId))));
		;
	}

	@Test
	public void testGetSearchLocations() throws Exception {
		final Integer locationDbId = RandomUtils.nextInt();
		final int searchResultsDbid = 1;
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationDbIds(Collections.singletonList(locationDbId));

		final Location location = this.getTestLocation();
		final List<Location> locationList = Collections.singletonList(location);

		doReturn(locationSearchRequest).when(this.searchRequestService).getSearchRequest(searchResultsDbid, LocationSearchRequest.class);
		doReturn(locationList).when(this.locationService)
			.getLocations(locationSearchRequest,
				new PageRequest(BrapiPagedResult.DEFAULT_PAGE_NUMBER, BrapiPagedResult.DEFAULT_PAGE_SIZE));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v2/search/locations/" + searchResultsDbid)
				.contentType(this.contentType)
				.locale(Locale.getDefault()))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(locationList.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationDbId",
				Matchers.is(location.getLocationDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationType",
				Matchers.is(location.getLocationType())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationName",
				Matchers.is(location.getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].abbreviation",
				Matchers.is(location.getAbbreviation())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryCode",
				Matchers.is(location.getCountryCode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryName",
				Matchers.is(location.getCountryName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].coordinates.geometry.coordinates",
				Matchers.is(location.getCoordinates().getGeometry().getCoordinates())));
	}

	private Location getTestLocation() {
		final String locType = "Breeding Location";
		final Location location = new Location("156", locType, "New Zealand", "NZL", "NZL",
			"NZ", 156.2, 58.6, 5.2, null, null);
		final Coordinate coordinates = new Coordinate();
		final Geometry geometry = new Geometry();
		geometry.setCoordinates(Arrays.asList(location.getLatitude(), location.getLongitude(), location.getAltitude()));
		coordinates.setGeometry(geometry);
		location.setCoordinates(coordinates);

		return location;
	}
}
