
package org.ibp.api.brapi.v1.location;

import java.util.List;

import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.beust.jcommander.internal.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class LocationResourceBrapiTest extends ApiUnitTestBase {

	private static final String MAIZE_BRAPI_V1_LOCATIONS = "/maize/brapi/v1/locations";

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public LocationService locationService() {
			return Mockito.mock(LocationService.class);
		}
	}

	@Autowired
	private LocationService locationService;

	@Test
	public void testListLocations() throws Exception {

		final String locType = "Breeding Location";
		final Location location = new Location("156", locType, "New Zealand", "NZL", "NZL", "NZ", 156.2, 58.6, 5.2, null, null);

		final List<Location> mwLocations = Lists.newArrayList(location);
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationId(location.getLocationDbId());
		Mockito.when(this.locationService.getLocations(locationSearchRequest, new PageRequest(0, 10))).thenReturn(mwLocations);
		Mockito.when(this.locationService.countLocations(locationSearchRequest)).thenReturn(1L);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(LocationResourceBrapiTest.MAIZE_BRAPI_V1_LOCATIONS)
				.queryParam("locationType", "country").build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwLocations.size()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationDbId", Matchers.is(location.getLocationDbId()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationType", Matchers.is(locType))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].name", Matchers.is(location.getName()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].abbreviation", Matchers.is(location.getAbbreviation()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryCode", Matchers.is(location.getCountryCode()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryName", Matchers.is(location.getCountryName()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].latitude", Matchers.is(location.getLatitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].longitude", Matchers.is(location.getLongitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].altitude", Matchers.is(location.getAltitude()))) //
				// Default starting page index is 0
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage",
						Matchers.is(BrapiPagedResult.DEFAULT_PAGE_NUMBER)))
				// Default page size is 1000
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(BrapiPagedResult.DEFAULT_PAGE_SIZE)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(1))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))) //
		;
	}
}
