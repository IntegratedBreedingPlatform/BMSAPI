
package org.ibp.api.domain.study.validators;

import java.util.Collections;

import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.ValidValues;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;

public class NumericVariablDataTypeValidatorTest {

	private NumericVariablDataTypeValidator numericVariableDataTypeValidator;

	@Before
	public void setup() {
		this.numericVariableDataTypeValidator = new NumericVariablDataTypeValidator();
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidDataTypeProvidedToTheNumericValidator() throws Exception {
		final MeasurementVariableDetails measurementVariableDetails =
				new MeasurementVariableDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.CHARACTER_DATA_TYPE,
						Mockito.mock(ValidValues.class), TestValidatorConstants.TEST_MEASUREMENT_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VALUE);
		this.numericVariableDataTypeValidator.validateValues(measurementVariableDetails, TestValidatorConstants.TEST_MEASUREMENT_INDEX,
				TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, Mockito.mock(Errors.class));
	}

	@Test
	public void testInvalidNumericValueProvidedByTheUser() throws Exception {
		this.numericVariableDataTypeValidator = new NumericVariablDataTypeValidator();
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final String[] invalidNumericValues = new String[] {"asdjlkfja", "100.10ffj", "110$", "787skdjf", "10wer10"};

		ValidationTestCommons.testInValidDataType(numericVariableDataTypeValidator, "invalid.measurement.numeric.value",
				TestValidatorConstants.NUMERIC_VARIABLE, mockValidValues, invalidNumericValues, "Invalid numeric value found.");
	}

	@Test
	public void testValidNumericValueProvidedByTheUser() throws Exception {
		this.numericVariableDataTypeValidator = new NumericVariablDataTypeValidator();
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final String[] invalidNumericValues =
				new String[] {"10.0", "10", "98239874298723497824398723498723497", "10.3847398", "10f", "10F", "100d", "100D", "100l",
						"100L", "100D", "3.30e23"};

		ValidationTestCommons.testValidDataType(numericVariableDataTypeValidator, TestValidatorConstants.NUMERIC_VARIABLE, mockValidValues,
				invalidNumericValues);
	}

}
