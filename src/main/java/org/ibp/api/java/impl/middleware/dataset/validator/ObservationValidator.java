package org.ibp.api.java.impl.middleware.dataset.validator;

import java.util.HashMap;

import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Component
public class ObservationValidator {
	
	@Autowired
	private DatasetService datasetService;
	
	private BindingResult errors;
	
	public void validateObservation(final Integer datasetId, final Integer observationUnitId) {
		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		
		final boolean isValid = this.datasetService.isValidObservationUnit(datasetId, observationUnitId);
		if (!isValid) {
			errors.reject("invalid.observation.unit.id", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		
	}

}
