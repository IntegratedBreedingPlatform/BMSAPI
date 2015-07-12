
package org.ibp.api.rest.location;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Georef;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.common.PagedResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class LocationResourceTest extends ApiUnitTestBase {

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
	public void testGetAllLocationTypes() throws Exception {

		UserDefinedField udfld1 = this.createTestLocTypeUdfld(415, "FIELD", "EXPERIMENTAL FIELD");
		UserDefinedField udfld2 = this.createTestLocTypeUdfld(416, "BLOCK", "FIELD BLOCK");

		List<UserDefinedField> mwLocTypes = new ArrayList<>();
		mwLocTypes.add(udfld1);
		mwLocTypes.add(udfld2);

		Mockito.when(
				this.locationDataManager.getUserDefinedFieldByFieldTableNameAndType(UDTableType.LOCATION_LTYPE.getTable(),
						UDTableType.LOCATION_LTYPE.getType())).thenReturn(mwLocTypes);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/location/{cropname}/types", "maize").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(mwLocTypes.size())))

				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['id']", Matchers.is(udfld1.getFldno().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['name']", Matchers.is(udfld1.getFcode())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['description']", Matchers.is(udfld1.getFname())))

				.andExpect(MockMvcResultMatchers.jsonPath("$[1]['id']", Matchers.is(udfld2.getFldno().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]['name']", Matchers.is(udfld2.getFcode())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]['description']", Matchers.is(udfld2.getFname())));
	}

	@Test
	public void testGetLocationsByType() throws Exception {
		String locationTypeId = "146";
		Mockito.when(this.locationDataManager.countLocationsByType(Integer.valueOf(locationTypeId))).thenReturn(1L);

		UserDefinedField udfldLocType = this.createTestLocTypeUdfld(415, "FIELD", "EXPERIMENTAL FIELD");
		Mockito.when(this.locationDataManager.getUserDefinedFieldByID(Integer.valueOf(locationTypeId))).thenReturn(udfldLocType);

		org.generationcp.middleware.pojos.Location mwLocation = this.createTestLocation();

		List<Location> mwLocationTypes = Lists.newArrayList(mwLocation);
		Mockito.when(this.locationDataManager.getLocationsByType(Integer.valueOf(locationTypeId), 0, PagedResult.DEFAULT_PAGE_SIZE))
				.thenReturn(mwLocationTypes);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/location/maize?locationTypeId=146").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults", IsCollectionWithSize.hasSize(mwLocationTypes.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].id", Matchers.is(mwLocation.getLocid().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].name", Matchers.is(mwLocation.getLname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].abbreviation", Matchers.is(mwLocation.getLabbr())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].latitude", Matchers.is(mwLocation.getLatitude())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].longitude", Matchers.is(mwLocation.getLongitude())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].altitude", Matchers.is(mwLocation.getAltitude())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0].locationType.id", Matchers.is(udfldLocType.getFldno().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].locationType.name", Matchers.is(udfldLocType.getFcode())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0].locationType.description", Matchers.is(udfldLocType.getFname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber", Matchers.is(PagedResult.DEFAULT_PAGE_NUMBER)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageSize", Matchers.is(PagedResult.DEFAULT_PAGE_SIZE)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalResults", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasNextPage", Matchers.is(false)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasPreviousPage", Matchers.is(false)));
	}

	@Test
	public void testSearchLocations() throws Exception {
		String searchString = "New";
		Mockito.when(this.locationDataManager.countLocationsByName(searchString, Operation.LIKE)).thenReturn(1L);

		UserDefinedField udfldLocType = this.createTestLocTypeUdfld(415, "FIELD", "EXPERIMENTAL FIELD");
		Mockito.when(this.locationDataManager.getUserDefinedFieldByID(org.mockito.Matchers.anyInt())).thenReturn(udfldLocType);

		org.generationcp.middleware.pojos.Location mwLocation = this.createTestLocation();

		List<Location> mwLocations = Lists.newArrayList(mwLocation);
		Mockito.when(this.locationDataManager.getLocationsByName(searchString, 0, PagedResult.DEFAULT_PAGE_SIZE, Operation.LIKE))
				.thenReturn(mwLocations);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/location/maize/search?q=" + searchString).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults", IsCollectionWithSize.hasSize(mwLocations.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].id", Matchers.is(mwLocation.getLocid().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].name", Matchers.is(mwLocation.getLname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].abbreviation", Matchers.is(mwLocation.getLabbr())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].latitude", Matchers.is(mwLocation.getLatitude())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].longitude", Matchers.is(mwLocation.getLongitude())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].altitude", Matchers.is(mwLocation.getAltitude())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0].locationType.id", Matchers.is(udfldLocType.getFldno().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].locationType.name", Matchers.is(udfldLocType.getFcode())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0].locationType.description", Matchers.is(udfldLocType.getFname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber", Matchers.is(PagedResult.DEFAULT_PAGE_NUMBER)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageSize", Matchers.is(PagedResult.DEFAULT_PAGE_SIZE)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalResults", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasNextPage", Matchers.is(false)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasPreviousPage", Matchers.is(false)));
	}

	private org.generationcp.middleware.pojos.Location createTestLocation() {
		org.generationcp.middleware.pojos.Location mwLocation = new Location();
		mwLocation.setLocid(156);
		mwLocation.setLname("New Zealand");
		mwLocation.setLabbr("NZL");
		mwLocation.setLtype(405);
		Georef georef = new Georef(156, 1, 41.17, 170.27, 10.11);
		mwLocation.setGeoref(georef);
		return mwLocation;
	}

	private UserDefinedField createTestLocTypeUdfld(Integer fieldNumber, String code, String name) {
		UserDefinedField udfldLocType = new UserDefinedField();
		udfldLocType.setFldno(fieldNumber);
		udfldLocType.setFcode(code);
		udfldLocType.setFname(name);
		return udfldLocType;
	}
}
