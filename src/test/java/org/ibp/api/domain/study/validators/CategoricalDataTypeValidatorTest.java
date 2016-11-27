
package org.ibp.api.domain.study.validators;

import java.util.Collections;

import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.ValidValues;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;

public class CategoricalDataTypeValidatorTest {

	private CategoricalDataTypeValidator categoricalDataTypeValidator;

	@Before
	public void setup() {
		this.categoricalDataTypeValidator = new CategoricalDataTypeValidator();
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidDataTypeProvidedToTheCategoricalValidator() throws Exception {
		final MeasurementDetails measurementVariableDetails =
				new MeasurementDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.CHARACTER_DATA_TYPE,
						Mockito.mock(ValidValues.class), TestValidatorConstants.TEST_MEASUREMENT_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VALUE);
		this.categoricalDataTypeValidator.validateValues(measurementVariableDetails,
				TestValidatorConstants.TEST_MEASUREMENT_INDEX, TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER,
				Mockito.mock(Errors.class));
	}

	@Test
	public void testVariableWithBlankCategoricalValues() throws Exception {
		final MeasurementDetails measurementVariableDetails =
				new MeasurementDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.CATEGORICAL_VARIABLE,
						Mockito.mock(ValidValues.class), TestValidatorConstants.TEST_MEASUREMENT_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VALUE);
		final Errors mockErrors = Mockito.mock(Errors.class);
		this.categoricalDataTypeValidator.validateValues(measurementVariableDetails, 0,
				TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, mockErrors);
		Mockito.verify(mockErrors).rejectValue(
				"Observation.measurement[" + TestValidatorConstants.TEST_MEASUREMENT_INDEX + "]",
				"variable.invalid.categorical.value",
				new Object[] {measurementVariableDetails.getMeasurementValue(), TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID, TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME},
						CategoricalDataTypeValidator.VARIABLE_CANNOT_HAVE_BLANK_CATEGORICAL_VALUES_DEFAULT_MESSAGE);
	}

	@Test
	public void testInvalidCategoricalValueProvidedByTheUser() throws Exception {
		this.categoricalDataTypeValidator = new CategoricalDataTypeValidator();
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final MeasurementDetails measurementVariableDetails =
				new MeasurementDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.CATEGORICAL_VARIABLE,
						mockValidValues, TestValidatorConstants.TEST_MEASUREMENT_ID, TestValidatorConstants.TEST_MEASUREMENT_VALUE);
		Mockito.when(mockValidValues.getCategories()).thenReturn(
				Collections.singletonList(new Category(TestValidatorConstants.TERM_SUMMART_ID, TestValidatorConstants.TERM_SUMMARY_NAME,
						TestValidatorConstants.TERM_SUMMARY_DEFINITION, Boolean.TRUE)));
		final Errors mockErrors = Mockito.mock(Errors.class);
		this.categoricalDataTypeValidator.validateValues(measurementVariableDetails, 0,
				TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, mockErrors);
		Mockito.verify(mockErrors).rejectValue(
				"measurementValue",
				"invalid.measurement.categorical.value",
				new Object[] {measurementVariableDetails.getMeasurementValue(), TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER,
						TestValidatorConstants.TEST_MEASUREMENT_ID, TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID},
				"Invalid categorical values found.");
	}

	@Test
	public void testValidCategoricalValueCausesNoException() throws Exception {
		this.categoricalDataTypeValidator = new CategoricalDataTypeValidator();
		final ValidValues mockValidValues = Mockito.mock(ValidValues.class);
		final MeasurementDetails measurementVariableDetails =
				new MeasurementDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.CATEGORICAL_VARIABLE,
						mockValidValues, TestValidatorConstants.TEST_MEASUREMENT_ID, TestValidatorConstants.TERM_SUMMARY_NAME);
		Mockito.when(mockValidValues.getCategories()).thenReturn(
				Collections.singletonList(new Category(TestValidatorConstants.TERM_SUMMART_ID, TestValidatorConstants.TERM_SUMMARY_NAME,
						TestValidatorConstants.TERM_SUMMARY_DEFINITION, Boolean.TRUE)));
		final Errors mockErrors = Mockito.mock(Errors.class);
		this.categoricalDataTypeValidator.validateValues(measurementVariableDetails, 0,
				TestValidatorConstants.TEST_OBSERVATION_IDENTIFIER, mockErrors);
	}

}
