package org.ibp.api.domain.study.validators;

import org.springframework.validation.Errors;


/**
 * Validate character measurement data
 *
 */
public class CharacterVariableDataTypeValidator implements DataTypeValidator {


	/* (non-Javadoc)
	 * @see org.ibp.api.domain.study.validators.DataTypeValidator#validateValues(org.ibp.api.domain.study.validators.MeasurementVariableDetails, int, int, org.springframework.validation.Errors)
	 */
	@Override
	public void validateValues(MeasurementDetails measurementVariableDetails, int measurementIndex, int observationId, Errors errors) {
		ensureCharacterDataType(measurementVariableDetails);
		if (measurementVariableDetails.getMeasurementValue().length() > 255) {
			errors.rejectValue("measurementValue","invalid.measurement.character.value", new Object[] {
					measurementVariableDetails.getMeasurementValue(), observationId, measurementVariableDetails.getMeasurementId()},
					"Invalid character value found.");
		}
	}

	private void ensureCharacterDataType(final MeasurementDetails measurementVariableDetails) {
		if (!(Integer.parseInt(measurementVariableDetails.getVariableDataType().getId()) == org.generationcp.middleware.domain.ontology.DataType.CHARACTER_VARIABLE
				.getId())) {
			throw new IllegalStateException("The ensureCharacterDataType method must never be called for non numeric variables. "
					+ "Please report this error to your administrator.");
		}
	}


}
