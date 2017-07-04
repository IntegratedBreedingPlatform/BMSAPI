
package org.ibp.api.brapi.v1.location;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Country;
import org.generationcp.middleware.pojos.LocdesType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.location.AdditionalInfoDto;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Before;
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
	public void testListLocationsWithOutAdditionalInfo() throws Exception {

		final String locType = "Breeding Location";
		final Integer locationTypeId = 410;
		final Integer countryId = 123;
		final LocationDetailsDto location1 = new LocationDetailsDto(156,locType,"New Zealand","NZL","NZL","NZ",156.2,58.6,5.2);

		final UserDefinedField locTypeUDFLD = new UserDefinedField(locationTypeId);
		locTypeUDFLD.setFname(locType);

		List<LocationDetailsDto> mwLocations = Lists.newArrayList(location1);
		Mockito.when(this.locationDataManager.getLocationsByFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyMapOf(LocationFilters.class, Object.class))).thenReturn(mwLocations);
		Mockito.when(this.locationDataManager.countLocationsByFilter(Mockito.anyMapOf(LocationFilters.class, Object.class))).thenReturn(1L);
		
		Mockito.when(this.locationDataManager.getUserDefinedFieldByID(locationTypeId)).thenReturn(locTypeUDFLD);

		Country country1 = new Country(countryId);
		country1.setIsothree("NZL");
		country1.setIsoabbr("NZ");
		Mockito.when(this.locationDataManager.getCountryById(countryId)).thenReturn(country1);
		
		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/locations?pageNumber=1&pageSize=10&locationType=country").contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwLocations.size()))) //
				.andExpect(jsonPath("$.result.data[0].locationDbId", Matchers.is(location1.getLocationDbId()))) //
				.andExpect(jsonPath("$.result.data[0].locationType", Matchers.is(locTypeUDFLD.getFname()))) //
				.andExpect(jsonPath("$.result.data[0].name", Matchers.is(location1.getName()))) //
				.andExpect(jsonPath("$.result.data[0].abbreviation", Matchers.is(location1.getAbbreviation()))) //
				.andExpect(jsonPath("$.result.data[0].countryCode", Matchers.is(country1.getIsothree()))) //
				.andExpect(jsonPath("$.result.data[0].countryName", Matchers.is(country1.getIsoabbr()))) //
				.andExpect(jsonPath("$.result.data[0].latitude", Matchers.is(location1.getLatitude()))) //
				.andExpect(jsonPath("$.result.data[0].longitude", Matchers.is(location1.getLongitude()))) //
				.andExpect(jsonPath("$.result.data[0].altitude", Matchers.is(location1.getAltitude()))) //
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", Matchers.is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.pageSize", Matchers.is(10))) //
				.andExpect(jsonPath("$.metadata.pagination.totalCount", Matchers.is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))) //
		;
	}

	@Test
	public void testListLocationsBadlocationType() throws Exception {
		Mockito.when(this.locationDataManager.getUserDefinedFieldIdOfName(org.generationcp.middleware.pojos.UDTableType.LOCATION_LTYPE,"countryy")).thenReturn(null);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/locations?pageNumber=1&pageSize=10&locationType=countryy").contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotFound()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.metadata.status.message", Matchers.is("not found locations"))); //

	}
	
	@Test
	public void testListLocationsWithAdditionalInfo() throws Exception {

		final String locType = "Breeding Location";
		final LocationDetailsDto location1 = new LocationDetailsDto(156,locType,"New Zealand","NZL","NZL","NZ",156.2,58.6,5.2);
		final LocationDetailsDto location2 = new LocationDetailsDto(100,locType,"Argentina","ARG","ARG","ARG",-34.7108688,-58.280082,24.000);

		AdditionalInfoDto additionalInfoDto = new AdditionalInfoDto(156);
		additionalInfoDto.addInfo("province","Auckland");
		location1.setMapAdditionalInfo(additionalInfoDto);

		additionalInfoDto = new AdditionalInfoDto(100);
		additionalInfoDto.addInfo("province","Buenos Aires");
		location2.setMapAdditionalInfo(additionalInfoDto);

		final Integer locationTypeId = 410;
		final UserDefinedField locTypeUDFLD = new UserDefinedField(locationTypeId);
		locTypeUDFLD.setFname(locType);
		
		final HashMap<Integer, AdditionalInfoDto> mapAdditionalInfo = new HashMap<>();
		mapAdditionalInfo.put(100, additionalInfoDto);
		List<LocationDetailsDto> mwLocations = Lists.newArrayList(location1, location2);
		Mockito.when(this.locationDataManager.getUserDefinedFieldByID(locationTypeId)).thenReturn(locTypeUDFLD);

		Mockito.when(this.locationDataManager.getLocationsByFilter(Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyMapOf(LocationFilters.class, Object.class))).thenReturn(mwLocations);
		Mockito.when(this.locationDataManager.countLocationsByFilter(Mockito.anyMapOf(LocationFilters.class, Object.class))).thenReturn(1L);
	
		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/locations?pageNumber=1&pageSize=10&locationType="+locType).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwLocations.size()))) //
				.andExpect(jsonPath("$.result.data[0].locationDbId", Matchers.is(location1.getLocationDbId()))) //
				.andExpect(jsonPath("$.result.data[0].locationType", Matchers.is(location1.getLocationType()))) //
				.andExpect(jsonPath("$.result.data[0].name", Matchers.is(location1.getName()))) //
				.andExpect(jsonPath("$.result.data[0].abbreviation", Matchers.is(location1.getAbbreviation()))) //
				.andExpect(jsonPath("$.result.data[0].countryCode", Matchers.is(location1.getCountryCode()))) //
				.andExpect(jsonPath("$.result.data[0].countryName", Matchers.is(location1.getCountryName()))) //
				.andExpect(jsonPath("$.result.data[0].latitude", Matchers.is(location1.getLatitude()))) //
				.andExpect(jsonPath("$.result.data[0].longitude", Matchers.is(location1.getLongitude()))) //
				.andExpect(jsonPath("$.result.data[0].altitude", Matchers.is(location1.getAltitude()))) //
				.andExpect(jsonPath("$.result.data[1].locationDbId", Matchers.is(location2.getLocationDbId()))) //
				.andExpect(jsonPath("$.result.data[1].locationType", Matchers.is(location2.getLocationType()))) //
				.andExpect(jsonPath("$.result.data[1].name", Matchers.is(location2.getName()))) //
				.andExpect(jsonPath("$.result.data[1].abbreviation", Matchers.is(location2.getAbbreviation()))) //
				.andExpect(jsonPath("$.result.data[1].countryCode", Matchers.is(location2.getCountryCode()))) //
				.andExpect(jsonPath("$.result.data[1].countryName", Matchers.is(location2.getCountryName()))) //
				.andExpect(jsonPath("$.result.data[1].latitude", Matchers.is(location2.getLatitude()))) //
				.andExpect(jsonPath("$.result.data[1].longitude", Matchers.is(location2.getLongitude()))) //
				.andExpect(jsonPath("$.result.data[1].altitude", Matchers.is(location2.getAltitude()))) //
				.andExpect(jsonPath("$.result.data[0].additionalInfo.province", Matchers.is(location1.getAdditionalInfo().getInfoValue("province")))) //
				.andExpect(jsonPath("$.result.data[1].additionalInfo.province", Matchers.is(location2.getAdditionalInfo().getInfoValue("province")))) //
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", Matchers.is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.pageSize", Matchers.is(10))) //
				.andExpect(jsonPath("$.metadata.pagination.totalCount", Matchers.is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))) //
		;
	}
}
