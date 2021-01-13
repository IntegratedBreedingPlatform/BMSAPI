package org.ibp.api.java.impl.middleware.location;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private ProgramValidator programValidator;

	@InjectMocks
	private LocationServiceImpl locationService;

	private static final String CROP = "maize";

	@Test
	public void testGetLocations_MissingProgramUUIDWhenFavoritesAreRequired() {
		try {
			this.locationService.getLocations(CROP, null, new HashSet<>(), new ArrayList<>(), new ArrayList<>(), true, null, null);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("locations.favorite.requires.program"));
		}
	}

	@Test
	public void testGetLocations_InvalidProgram() {
		try {
			this.locationService.getLocations(CROP, "", new HashSet<>(), new ArrayList<>(), new ArrayList<>(), false, null, null);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("program.does.not.exist"));
		}
	}

	@Test
	public void testGetLocations_Ok() {
		final String programUUID = "myprogram";
		org.generationcp.middleware.pojos.Location locationMw = new Location();
		final String locationName = RandomStringUtils.randomAlphabetic(10);
		final String locationAbbreviation = RandomStringUtils.randomAlphabetic(3);
		locationMw.setLname(locationName);
		locationMw.setLabbr(locationAbbreviation);

		Mockito.when(locationDataManager
			.getFilteredLocations(Mockito.eq(programUUID), Mockito.anySet(), Mockito.anyList(), Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.any()))
			.thenReturn(
				Collections.singletonList(locationMw));

		final List<LocationDto> locationList =
			this.locationService.getLocations(CROP, programUUID, new HashSet<>(), new ArrayList<>(), new ArrayList<>(), false, "", null);

		assertThat(locationList, hasSize(1));
		assertThat(locationList.get(0).getName(), equalTo(locationName));
		assertThat(locationList.get(0).getAbbreviation(), equalTo(locationAbbreviation));
	}

}
