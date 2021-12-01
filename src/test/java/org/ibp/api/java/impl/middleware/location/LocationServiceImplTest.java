package org.ibp.api.java.impl.middleware.location;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.pojos.Location;
import org.ibp.api.java.impl.middleware.location.validator.LocationSearchRequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
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
		Mockito.when(this.locationMiddlewareService.countFilteredLocations(Mockito.any()))
			.thenReturn(1l);

		final long count =
			this.locationService.countLocations(CROP, new LocationSearchRequest());

		Mockito.verify(this.locationSearchRequestValidator).validate(Mockito.any(), Mockito.any());
		assertThat(count, equalTo(1l));
	}

	@Test
	public void testGetLocations() {
		org.generationcp.middleware.pojos.Location locationMw = new Location();
		final String locationName = RandomStringUtils.randomAlphabetic(10);
		final String locationAbbreviation = RandomStringUtils.randomAlphabetic(3);
		locationMw.setLname(locationName);
		locationMw.setLabbr(locationAbbreviation);

		Mockito.when(this.locationMiddlewareService.getFilteredLocations(Mockito.any(), Mockito.any()))
			.thenReturn(
				Collections.singletonList(locationMw));

		final List<LocationDTO> locationList =
			this.locationService.getLocations(CROP, new LocationSearchRequest(), null);

		Mockito.verify(this.locationSearchRequestValidator).validate(Mockito.any(), Mockito.any());
		assertThat(locationList, hasSize(1));
		assertThat(locationList.get(0).getName(), equalTo(locationName));
		assertThat(locationList.get(0).getAbbreviation(), equalTo(locationAbbreviation));
	}

}
