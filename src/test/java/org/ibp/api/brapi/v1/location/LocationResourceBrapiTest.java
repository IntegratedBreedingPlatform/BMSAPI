
package org.ibp.api.brapi.v1.location;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Country;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.api.brapi.v1.location.AdditionalInfoDto;
import org.generationcp.middleware.api.brapi.v1.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
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

import com.beust.jcommander.internal.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class LocationResourceBrapiTest extends ApiUnitTestBase {

	private static final String MAIZE_BRAPI_V1_LOCATIONS = "/maize/brapi/v1/locations";

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
	public void testListLocationsWithOutAdditionalInfo() throws Exception {

		final String locType = "Breeding Location";
		final Integer locationTypeId = 410;
		final Integer countryId = 123;
		final LocationDetailsDto location1 = new LocationDetailsDto(156, locType, "New Zealand", "NZL", "NZL", "NZ", 156.2, 58.6, 5.2);

		final UserDefinedField locTypeUDFLD = new UserDefinedField(locationTypeId);
		locTypeUDFLD.setFname(locType);

		final List<LocationDetailsDto> mwLocations = Lists.newArrayList(location1);
		Mockito.when(this.locationDataManager.getLocationsByFilter(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.anyMapOf(LocationFilters.class, Object.class))).thenReturn(mwLocations);
		Mockito.when(this.locationDataManager.countLocationsByFilter(org.mockito.Matchers.anyMapOf(LocationFilters.class, Object.class)))
				.thenReturn(1L);

		Mockito.when(this.locationDataManager.getUserDefinedFieldByID(locationTypeId)).thenReturn(locTypeUDFLD);

		final Country country1 = new Country(countryId);
		country1.setIsothree("NZL");
		country1.setIsoabbr("NZ");
		Mockito.when(this.locationDataManager.getCountryById(countryId)).thenReturn(country1);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(LocationResourceBrapiTest.MAIZE_BRAPI_V1_LOCATIONS)
				.queryParam("locationType", "country").build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwLocations.size()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationDbId", Matchers.is(location1.getLocationDbId().toString()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationType", Matchers.is(locTypeUDFLD.getFname()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].name", Matchers.is(location1.getName()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].abbreviation", Matchers.is(location1.getAbbreviation()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryCode", Matchers.is(country1.getIsothree()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryName", Matchers.is(country1.getIsoabbr()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].latitude", Matchers.is(location1.getLatitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].longitude", Matchers.is(location1.getLongitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].altitude", Matchers.is(location1.getAltitude()))) //
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

	@Test
	public void testListLocationsBadlocationType() throws Exception {
		Mockito.when(this.locationDataManager.getUserDefinedFieldIdOfName(org.generationcp.middleware.pojos.UDTableType.LOCATION_LTYPE,
				"countryy")).thenReturn(null);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(LocationResourceBrapiTest.MAIZE_BRAPI_V1_LOCATIONS)
				.queryParam("locationType", "countryy").build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isNotFound()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0].message", Matchers.is("not found locations"))); //

	}

	@Test
	public void testListLocationsWithAdditionalInfo() throws Exception {

		final String locType = "Breeding Location";
		final LocationDetailsDto location1 = new LocationDetailsDto(156, locType, "New Zealand", "NZL", "NZL", "NZ", 156.2, 58.6, 5.2);
		final LocationDetailsDto location2 =
				new LocationDetailsDto(100, locType, "Argentina", "ARG", "ARG", "ARG", -34.7108688, -58.280082, 24.000);
		final LocationDetailsDto location3 =
				new LocationDetailsDto(123, locType, "Philippines", "PHL", "PHL", "PHL", 14.5995, 120.948, 5.0);

		AdditionalInfoDto additionalInfoDto = new AdditionalInfoDto(156);
		additionalInfoDto.addInfo("province", "Auckland");
		location1.setMapAdditionalInfo(additionalInfoDto);

		additionalInfoDto = new AdditionalInfoDto(100);
		additionalInfoDto.addInfo("province", "Buenos Aires");
		location2.setMapAdditionalInfo(additionalInfoDto);

		additionalInfoDto = new AdditionalInfoDto(100);
		additionalInfoDto.addInfo("province", "Manila");
		location3.setMapAdditionalInfo(additionalInfoDto);

		final Integer locationTypeId = 410;
		final UserDefinedField locTypeUDFLD = new UserDefinedField(locationTypeId);
		locTypeUDFLD.setFname(locType);

		final HashMap<Integer, AdditionalInfoDto> mapAdditionalInfo = new HashMap<>();
		mapAdditionalInfo.put(100, additionalInfoDto);
		final List<LocationDetailsDto> mwLocations = Lists.newArrayList(location1, location2, location3);
		Mockito.when(this.locationDataManager.getUserDefinedFieldByID(locationTypeId)).thenReturn(locTypeUDFLD);

		Mockito.when(this.locationDataManager.getLocationsByFilter(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.anyMapOf(LocationFilters.class, Object.class))).thenReturn(mwLocations);
		Mockito.when(this.locationDataManager.countLocationsByFilter(org.mockito.Matchers.anyMapOf(LocationFilters.class, Object.class)))
				.thenReturn(new Long(mwLocations.size()));

		final int pageNumber = 1;
		final int pageSize = 2;
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(LocationResourceBrapiTest.MAIZE_BRAPI_V1_LOCATIONS)
				.queryParam("page", pageNumber).queryParam("pageSize", pageSize).queryParam("locationType", locType).build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwLocations.size()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationDbId", Matchers.is(location1.getLocationDbId().toString()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].locationType", Matchers.is(location1.getLocationType()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].name", Matchers.is(location1.getName()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].abbreviation", Matchers.is(location1.getAbbreviation()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryCode", Matchers.is(location1.getCountryCode()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryName", Matchers.is(location1.getCountryName()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].latitude", Matchers.is(location1.getLatitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].longitude", Matchers.is(location1.getLongitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].altitude", Matchers.is(location1.getAltitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].locationDbId", Matchers.is(location2.getLocationDbId().toString()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].locationType", Matchers.is(location2.getLocationType()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].name", Matchers.is(location2.getName()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].abbreviation", Matchers.is(location2.getAbbreviation()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].countryCode", Matchers.is(location2.getCountryCode()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].countryName", Matchers.is(location2.getCountryName()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].latitude", Matchers.is(location2.getLatitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].longitude", Matchers.is(location2.getLongitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].altitude", Matchers.is(location2.getAltitude()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo.province",
						Matchers.is(location1.getAdditionalInfo().getInfoValue("province")))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].additionalInfo.province",
						Matchers.is(location2.getAdditionalInfo().getInfoValue("province")))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage", Matchers.is(pageNumber)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(pageSize))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(mwLocations.size()))) //
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(2))) //
		;
	}
}
