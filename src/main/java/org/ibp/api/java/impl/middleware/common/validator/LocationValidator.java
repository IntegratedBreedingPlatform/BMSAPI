package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Lists;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;

@Component
public class LocationValidator {

	@Autowired
	private LocationDataManager locationDataManager;

	public void validateSeedLocationId(final BindingResult errors, final Integer locationId) {
		if (locationId == null) {
			errors.reject("location.required", "");
			return;
		}
		final Location location = locationDataManager.getLocationByID(locationId);
		if (location == null) {
			errors.reject("location.invalid", "");
		} else {
			final List<Location> locationList = this.locationDataManager.getAllSeedingLocations(Lists.newArrayList(locationId));
			if (locationList.isEmpty()) {
				errors.reject("seed.location.invalid", "");
			}
		}
	}

}
