
package org.ibp.api.domain.study.validators;

import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.ValidValues;
import org.mockito.Mockito;
import org.springframework.validation.Errors;

public class ValidationTestCommons {

	static void testInValidDataType(final DataTypeValidator dataTypeValidator, final String key, final DataType dataType, final ValidValues mockValidValues,
			final String[] invalidNumericValues, final String defaultErrorMessage) {
		for (String invalidNumericValue : invalidNumericValues) {
			final MeasurementVariableDetails measurementVariableDetails =
					new MeasurementVariableDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
							TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, dataType,
							mockValidValues, TestValidatorConstants.TEST_MEASUREMENT_ID, invalidNumericValue);
			final Errors mockErrors = Mockito.mock(Errors.class);
			dataTypeValidator.validateValues(measurementVariableDetails, 0, TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, mockErrors);
			Mockito.verify(mockErrors).rejectValue(
					"measurementValue",
					key,
					new Object[] {measurementVariableDetails.getMeasurementValue(), TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER,
							TestValidatorConstants.TEST_MEASUREMENT_ID}, defaultErrorMessage);
			Mockito.reset(mockErrors);
		}
	}

	static void testValidDataType(final DataTypeValidator dataTypeValidator, final DataType dataType, final ValidValues mockValidValues,
			final String[] invalidNumericValues) {
		for (String invalidNumericValue : invalidNumericValues) {
			final MeasurementVariableDetails measurementVariableDetails =
					new MeasurementVariableDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
							TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, dataType,
							mockValidValues, TestValidatorConstants.TEST_MEASUREMENT_ID, invalidNumericValue);
			final Errors mockErrors = Mockito.mock(Errors.class);
			dataTypeValidator.validateValues(measurementVariableDetails, 0, TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, mockErrors);
			Mockito.verify(mockErrors, Mockito.times(0)).rejectValue(Mockito.anyString(), Mockito.anyString(), Mockito.any(Object[].class),
					Mockito.anyString());
			Mockito.reset(mockErrors);
		}
	}
}
