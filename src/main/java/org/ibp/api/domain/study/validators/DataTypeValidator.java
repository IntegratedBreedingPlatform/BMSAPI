
package org.ibp.api.domain.study.validators;

import org.springframework.validation.Errors;

/**
 * Observation data type based validation
 *
 */
public interface DataTypeValidator {

	/**
	 * Validates observations according to its data type
	 * @param measurementVariableDetails observations variable details
	 * @param measurementIndex the array index so that we can construct an appropriate error message
	 * @param observationId the observationId we are do the validation for
	 * @param errors All errors are updated into this collection. 
	 */
	public void validateValues(MeasurementVariableDetails measurementVariableDetails, int measurementIndex, int observationId,
			Errors errors);

}
