
package org.ibp.api.domain.study.validators;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.validation.Errors;

/**
 * Validate numeric measurement data.
 *
 */
public class NumericVariablDataTypeValidator implements DataTypeValidator {

	@Override
	public void validateValues(final MeasurementVariableDetails measurementVariableDetails, final int measurementIndex,
			final int observationId, final Errors errors) {
		ensureNumbericDataType(measurementVariableDetails); 
		if(!NumberUtils.isNumber(measurementVariableDetails.getMeasurementValue().trim())) {
			errors.rejectValue(
					"Observation.measurement[" + measurementIndex + "]",
					"invalid.measurement.numeric.value",
					new Object[] {measurementVariableDetails.getMeasurementValue(), observationId,
							measurementVariableDetails.getMeasurementId()},
					"Invalid numeric value found.");
		}
	}

	private void ensureNumbericDataType(final MeasurementVariableDetails measurementVariableDetails) {
		if (!(Integer.parseInt(measurementVariableDetails.getVariableDataType().getId()) == org.generationcp.middleware.domain.ontology.DataType.NUMERIC_VARIABLE
				.getId())) {
			throw new IllegalStateException("The ensureNumbericDataType method must never be called for non numeric variables. "
					+ "Please report this error to your administrator.");
		}
	}
}
