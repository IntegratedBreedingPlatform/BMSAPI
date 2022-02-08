package org.ibp.api.java.impl.middleware.location;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.ibp.api.java.impl.middleware.location.validator.LocationSearchRequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {

	@Mock
	private LocationSearchRequestValidator locationSearchRequestValidator;

	@Mock
	private LocationService locationMiddlewareService;

	@InjectMocks
	private LocationServiceImpl locationService;

	private static final String CROP = "maize";

	@Test
	public void countLocations() {
		Mockito.when(this.locationMiddlewareService.countFilteredLocations(Mockito.any(), ArgumentMatchers.isNull()))
			.thenReturn(1l);

		final long count =
			this.locationService.countLocations(CROP, new LocationSearchRequest(), null);

		assertThat(count, equalTo(1l));
	}

	@Test
	public void testGetLocations() {
		LocationDTO locationMw = new LocationDTO();
		final String locationName = RandomStringUtils.randomAlphabetic(10);
		final String locationAbbreviation = RandomStringUtils.randomAlphabetic(3);
		locationMw.setName(locationName);
		locationMw.setAbbreviation(locationAbbreviation);

		Mockito.when(this.locationMiddlewareService.searchLocations(Mockito.any(), Mockito.any(), Mockito.isNull()))
			.thenReturn(
				Collections.singletonList(locationMw));

		final List<LocationDTO> locationList =
			this.locationService.searchLocations(CROP, new LocationSearchRequest(), null, null);

		assertThat(locationList, hasSize(1));
		assertThat(locationList.get(0).getName(), equalTo(locationName));
		assertThat(locationList.get(0).getAbbreviation(), equalTo(locationAbbreviation));
	}

}
