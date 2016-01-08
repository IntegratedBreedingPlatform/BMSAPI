
package org.ibp.api.domain.study.validators;

import org.ibp.api.domain.ontology.ValidValues;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;

public class CharacterVariableDataTypeValidatorTest {

	private CharacterVariableDataTypeValidator characterVariableDataTypeValidator;

	@Before
	public void setUp() throws Exception {
		characterVariableDataTypeValidator = new CharacterVariableDataTypeValidator();
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidDataTypeProvidedToTheNumericValidator() throws Exception {
		final MeasurementDetails measurementVariableDetails =
				new MeasurementDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.NUMERIC_VARIABLE,
						Mockito.mock(ValidValues.class), TestValidatorConstants.TEST_MEASUREMENT_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VALUE);
		this.characterVariableDataTypeValidator.validateValues(measurementVariableDetails, TestValidatorConstants.TEST_MEASUREMENT_INDEX,
				TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, Mockito.mock(Errors.class));
	}

	@Test
	public void testInvalidDateValueProvidedByTheUser() throws Exception {
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final String[] invalidNumericValues = new String[] {"This is a very long line more than 255 characters long. See me goooooooooooooooooooooooooooooooooooooooo"
				+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooo000000000000000000000000888888888888888888888888888888888888888888888888888888888"
				+ "123456789012345", };

		ValidationTestCommons.testInValidDataType(characterVariableDataTypeValidator, "invalid.measurement.character.value",
				TestValidatorConstants.CHARACTER_DATA_TYPE, mockValidValues, invalidNumericValues, "Invalid character value found.");
	}

	@Test
	public void testValidDateValueProvidedByTheUser() throws Exception {
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final String[] invalidNumericValues = new String[] {"This can be anything at all", "&$$&%&%&"};

		ValidationTestCommons.testValidDataType(characterVariableDataTypeValidator, TestValidatorConstants.CHARACTER_DATA_TYPE,
				mockValidValues, invalidNumericValues);
	}
}
