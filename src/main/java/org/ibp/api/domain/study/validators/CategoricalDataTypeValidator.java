
package org.ibp.api.domain.study.validators;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ibp.api.domain.ontology.TermSummary;
import org.springframework.validation.Errors;

/**
 * Validate categorical measurement data
 *
 */
public class CategoricalDataTypeValidator implements DataTypeValidator {

	static final String VARIABLE_CANNOT_HAVE_BLANK_CATEGORICAL_VALUES_DEFAULT_MESSAGE = "Variable cannot have blank categorical values.";

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ibp.api.domain.study.validators.DataTypeValidator#validateCategoricalValues(org.ibp.api.domain.study.validators.
	 * MeasurementVariableDetails, int, int, org.springframework.validation.Errors)
	 */
	@Override
	public void validateValues(final MeasurementDetails measurementVariableDetails, final int measurementIndex,
			final int observationId, final Errors errors) {

		this.ensureCategoricalValue(measurementVariableDetails);

		final Map<String, TermSummary> mappedCategories = measurementVariableDetails.getMappedCategories();
		if (!mappedCategories.isEmpty()) {
			if (StringUtils.isNotBlank(measurementVariableDetails.getMeasurementValue())) {
				final TermSummary termSummary = mappedCategories.get(measurementVariableDetails.getMeasurementValue().trim());
				if (termSummary == null) {
					errors.rejectValue("measurementValue",
							"invalid.measurement.categorical.value",
							new Object[] {measurementVariableDetails.getMeasurementValue(), observationId,
									measurementVariableDetails.getMeasurementId(), measurementVariableDetails.getVariableId()},
							"Invalid categorical values found.");
				}
			}
		} else {
			errors.rejectValue("Observation.measurement[" + measurementIndex + "]", "variable.invalid.categorical.value", new Object[] {
					measurementVariableDetails.getMeasurementValue(), observationId, measurementVariableDetails.getVariableId(),
					measurementVariableDetails.getVariableName()},
					CategoricalDataTypeValidator.VARIABLE_CANNOT_HAVE_BLANK_CATEGORICAL_VALUES_DEFAULT_MESSAGE);
		}
	}

	private void ensureCategoricalValue(final MeasurementDetails measurementVariableDetails) {
		if (!(Integer.parseInt(measurementVariableDetails.getVariableDataType().getId()) == org.generationcp.middleware.domain.ontology.DataType.CATEGORICAL_VARIABLE
				.getId())) {
			throw new IllegalStateException("The validateCategoricalValues method must never be called for non categorical variables. "
					+ "Please report this error to your administrator.");
		}
	}

}
