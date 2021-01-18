package org.ibp.api.java.impl.middleware.location.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class LocationSearchRequestValidator {

	@Autowired
	private ProgramValidator programValidator;

	public void validate(final String crop, final LocationSearchRequest locationSearchRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		if (locationSearchRequest.getFavourites() && StringUtils.isEmpty(locationSearchRequest.getProgramUUID())) {
			errors.reject("locations.favorite.requires.program", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (locationSearchRequest.getProgramUUID() != null) {
			this.programValidator.validate(new ProgramSummary(crop, locationSearchRequest.getProgramUUID()), errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

}
