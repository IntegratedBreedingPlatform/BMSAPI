package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class LocationValidator {

	@Autowired
	private LocationDataManager locationDataManager;

	public void validateLocationId(final BindingResult errors, final Integer locationId) {
		if (locationId == null) {
			errors.reject("location.required", "");
			return;
		}
		final Location location = locationDataManager.getLocationByID(locationId);
		if (location == null) {
			errors.reject("location.invalid", "");
		}
	}

}
