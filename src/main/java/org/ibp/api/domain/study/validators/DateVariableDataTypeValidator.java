
package org.ibp.api.domain.study.validators;

import java.text.DateFormat;

import org.apache.commons.validator.routines.DateValidator;
import org.springframework.validation.Errors;

/**
 * Validate date measurement data
 *
 */
public class DateVariableDataTypeValidator implements DataTypeValidator {

	private DateValidator dateValidator;

	public DateVariableDataTypeValidator() {
		dateValidator = new DateValidator(false, DateFormat.SHORT);
	}

	@Override
	public void validateValues(MeasurementVariableDetails measurementVariableDetails, int measurementIndex, int observationId, Errors errors) {
		ensureDateDataType(measurementVariableDetails);
		if (!dateValidator.isValid(measurementVariableDetails.getMeasurementValue().trim(), "yyyyMMdd")) {
			errors.rejectValue("Observation.measurement[" + measurementIndex + "]", "invalid.measurement.date.value", new Object[] {
					measurementVariableDetails.getMeasurementValue(), observationId, measurementVariableDetails.getMeasurementId()},
					"Invalid date value found.");
		}
	}

	private void ensureDateDataType(final MeasurementVariableDetails measurementVariableDetails) {
		if (!(Integer.parseInt(measurementVariableDetails.getVariableDataType().getId()) == org.generationcp.middleware.domain.ontology.DataType.DATE_TIME_VARIABLE
				.getId())) {
			throw new IllegalStateException("The ensureDateDataType method must never be called for non numeric variables. "
					+ "Please report this error to your administrator.");
		}
	}

}
