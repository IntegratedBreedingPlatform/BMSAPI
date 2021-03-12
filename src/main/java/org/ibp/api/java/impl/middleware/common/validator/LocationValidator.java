package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.ibp.api.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LocationValidator {

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private LocationService locationService;

	public void validateSeedLocationId(final BindingResult errors, final String programUUID, final Integer locationId) {
		if (locationId == null) {
			errors.reject("location.required", "");
			return;
		}
		final Location location = locationDataManager.getLocationByID(locationId);
		if (location == null) {
			errors.reject("location.invalid", "");
			return;
		}
		if (!StringUtils.isEmpty(programUUID) && location.getProgramUUID() != null && !location.getProgramUUID().equals(programUUID)) {
			errors.reject("location.belongs.to.another.program", "");
			return;
		}
		final List<Location> locationList = this.locationDataManager.getAllSeedingLocations(Lists.newArrayList(locationId));
		if (locationList.isEmpty()) {
			errors.reject("seed.location.invalid", "");
			return;
		}
	}

	public void validateSeedLocationAbbr(final BindingResult errors, final String programUUID, final List<String> locationAbbreviations) {
		if (locationAbbreviations.stream().anyMatch(s -> StringUtils.isBlank(s))) {
			errors.reject("lot.input.list.location.null.or.empty", "");
			return;
		}

		final List<Location> existingLocations =
			this.locationService.getFilteredLocations(
				new LocationSearchRequest(programUUID, STORAGE_LOCATION_TYPE, null, locationAbbreviations, null, false), null);
		if (existingLocations.size() != locationAbbreviations.size()) {

			final List<String> existingAbbreviations = existingLocations.stream().map(Location::getLabbr).collect(Collectors.toList());
			final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
			invalidAbbreviations.removeAll(existingAbbreviations);
			errors.reject("lot.input.invalid.abbreviations", new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
		}
	}

	public void validateLocation(final BindingResult errors, final Integer locationId) {
		if (locationId == null) {
			errors.reject("location.required", "");
			return;
		}

		final Location location = locationDataManager.getLocationByID(locationId);

		if (location == null) {
			errors.reject("location.invalid", "");
			return;
		}
	}
}
