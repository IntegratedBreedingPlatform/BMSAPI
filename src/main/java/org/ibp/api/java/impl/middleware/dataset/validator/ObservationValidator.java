package org.ibp.api.java.impl.middleware.dataset.validator;

import java.util.HashMap;

import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Component("datasetObservationValidator")
public class ObservationValidator {
	
	@Autowired
	private DatasetService datasetService;
	
	private BindingResult errors;
	
	public void validateObservationUnit(final Integer datasetId, final Integer observationUnitId) {
		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		
		final boolean isValid = this.datasetService.isValidObservationUnit(datasetId, observationUnitId);
		if (!isValid) {
			errors.reject("invalid.observation.unit.id", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
		
	}
	
	public void validateObservation(final Integer datasetId, final Integer observationUnitId, final Integer observationId) {
		this.validateObservationUnit(datasetId, observationUnitId);
		
		final boolean isValid = this.datasetService.isValidObservation(observationUnitId, observationId);
		if (!isValid) {
			errors.reject("invalid.observation.id", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
		
		
	}

}
