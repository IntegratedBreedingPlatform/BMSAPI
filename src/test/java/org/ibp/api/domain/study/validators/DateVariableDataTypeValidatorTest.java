
package org.ibp.api.domain.study.validators;

import org.ibp.api.domain.ontology.ValidValues;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;

public class DateVariableDataTypeValidatorTest {

	private DateVariableDataTypeValidator dateVariableDataTypeValidator;

	@Before
	public void setup() {
		this.dateVariableDataTypeValidator = new DateVariableDataTypeValidator();
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidDataTypeProvidedToTheNumericValidator() throws Exception {
		final MeasurementVariableDetails measurementVariableDetails =
				new MeasurementVariableDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.CHARACTER_DATA_TYPE,
						Mockito.mock(ValidValues.class), TestValidatorConstants.TEST_MEASUREMENT_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VALUE);
		this.dateVariableDataTypeValidator.validateValues(measurementVariableDetails, TestValidatorConstants.TEST_MEASUREMENT_INDEX,
				TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, Mockito.mock(Errors.class));
	}

	@Test
	public void testInvalidDateValueProvidedByTheUser() throws Exception {
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final String[] invalidNumericValues = new String[] {"asdjlkfja", "100.10ffj", "110$", "787skdjf", "10wer10"};

		ValidationTestCommons.testInValidDataType(dateVariableDataTypeValidator, "invalid.measurement.date.value",
				TestValidatorConstants.DATE_TIME_VARIABLE, mockValidValues, invalidNumericValues, "Invalid date value found.");
	}

	@Test
	public void testValidDateValueProvidedByTheUser() throws Exception {
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final String[] invalidNumericValues = new String[] {"20101111", "19700101", "1965011", "1965119"};

		ValidationTestCommons.testValidDataType(dateVariableDataTypeValidator, TestValidatorConstants.DATE_TIME_VARIABLE, mockValidValues,
				invalidNumericValues);
	}

}
