package org.ibp.api.brapi.v1.location;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Country;
import org.generationcp.middleware.pojos.Georef;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.UserDefinedField;
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

import com.beust.jcommander.internal.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class LocationResourceBrapiTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public LocationDataManager locationDataManager() {
			return Mockito.mock(LocationDataManager.class);
		}
	}

	@Autowired
	private LocationDataManager locationDataManager;

	@Test
	public void testListLocations() throws Exception {

		Integer countryId = 123;
		Location location1 = new Location();
		location1.setCntryid(countryId);
		location1.setLocid(156);
		location1.setLname("New Zealand");
		location1.setLtype(405);
		location1.setLabbr("NZ");
		location1.setGeoref(new Georef(156, 1, 41.17, 170.27, 10.11));

		List<Location> mwLocations = Lists.newArrayList(location1);
		Mockito.when(this.locationDataManager.getAllLocalLocations(Mockito.anyInt(), Mockito.anyInt())).thenReturn(mwLocations);
		Mockito.when(this.locationDataManager.countAllLocations()).thenReturn(200L);
		
		final UserDefinedField locTypeUDFLD = new UserDefinedField(location1.getLtype());
		locTypeUDFLD.setFname("Breeding Location");
		Mockito.when(this.locationDataManager.getUserDefinedFieldByID(location1.getLtype())).thenReturn(locTypeUDFLD);

		Country country1 = new Country(countryId);
		country1.setIsothree("NZL");
		country1.setIsoabbr("NZ");
		Mockito.when(this.locationDataManager.getCountryById(countryId)).thenReturn(country1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/locations?pageNumber=1&pageSize=10").contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwLocations.size()))) //
				.andExpect(jsonPath("$.result.data[0].locationDbId", Matchers.is(location1.getLocid()))) //
				.andExpect(jsonPath("$.result.data[0].locationType", Matchers.is(locTypeUDFLD.getFname()))) //
				.andExpect(jsonPath("$.result.data[0].name", Matchers.is(location1.getLname()))) //
				.andExpect(jsonPath("$.result.data[0].abbreviation", Matchers.is(location1.getLabbr()))) //
				.andExpect(jsonPath("$.result.data[0].countryCode", Matchers.is(country1.getIsothree()))) //
				.andExpect(jsonPath("$.result.data[0].countryName", Matchers.is(country1.getIsoabbr()))) //
				.andExpect(jsonPath("$.result.data[0].latitude", Matchers.is(location1.getLatitude()))) //
				.andExpect(jsonPath("$.result.data[0].longitude", Matchers.is(location1.getLongitude()))) //
				.andExpect(jsonPath("$.result.data[0].altitude", Matchers.is(location1.getAltitude()))) //
				.andExpect(jsonPath("$.result.data[0].attributes", Matchers.is(IsCollectionWithSize.hasSize(0))))
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", Matchers.is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.pageSize", Matchers.is(10))) //
				.andExpect(jsonPath("$.metadata.pagination.totalCount", Matchers.is(200))) //
				.andExpect(jsonPath("$.metadata.pagination.totalPages", Matchers.is(20))) //
		;
	}

}
