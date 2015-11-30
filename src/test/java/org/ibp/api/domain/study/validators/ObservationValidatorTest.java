
package org.ibp.api.domain.study.validators;

import java.util.Collections;
import java.util.Map;

import org.ibp.api.domain.common.BmsRequestAttributes;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.validation.Errors;

public class ObservationValidatorTest {

	/**
	 * Ensure that categorical values and valid i.e. makes sure that when the data type is categorical only appropriate values are allowed.
	 *
	 */
	@Test
	public void testCategorialValuesValidated() throws Exception {
		// ObservationValidator observationValidator = new ObservationValidator();
		// Measurement measurement = new Measurement();
		// Trait trait = new Trait(1, "Test Categorical Trait");
		// MeasurementIdentifier measurementIdentifier = new MeasurementIdentifier(1, trait);
		// measurement.setMeasurementIdentifier(measurementIdentifier);
		// measurement.setMeasurementValue("30000"); //categorical value id
		// observationValidator.validateCategoricalValues(measurement);
	}

	@Test
	public void testValidate() throws Exception {
		final Observation mockObservation = Mockito.mock(Observation.class);

		final VariableService mockVariableService = Mockito.mock(VariableService.class);
		final BmsRequestAttributes mockBmsRequestAttributes = Mockito.mock(BmsRequestAttributes.class);
		final ObservationValidationDataExtractor mockObservationValidationDataExtractor =
				Mockito.mock(ObservationValidationDataExtractor.class);
		final MeasurementDataValidatorFactory mockMeasurementDataValidatoyFactory = Mockito.mock(MeasurementDataValidatorFactory.class);
		final ObservationValidationData mockObservationValidationData = Mockito.mock(ObservationValidationData.class);
		// We are using any object because everything is mocked. Not really looking to check any values being mapped
		Mockito.when(
				mockObservationValidationDataExtractor.getObservationValidationData(Matchers.<Observation>anyObject(),
						Matchers.<Map<?, ?>>anyObject(), Matchers.<VariableService>anyObject())).thenReturn(mockObservationValidationData);

		final MeasurementVariableDetails measurementVariableDetails =
				new MeasurementVariableDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_NAME, TestValidatorConstants.CHARACTER_DATA_TYPE,
						Mockito.mock(ValidValues.class), TestValidatorConstants.TEST_MEASUREMENT_ID,
						TestValidatorConstants.TEST_MEASUREMENT_VALUE);
		Mockito.when(mockObservationValidationData.getMeasurementVariableDetailsList()).thenReturn(
				Collections.singletonMap(0, measurementVariableDetails));

		final DataTypeValidator mockDataTypeValidator = Mockito.mock(DataTypeValidator.class);
		Mockito.when(mockMeasurementDataValidatoyFactory.getMeasurementValidator(TestValidatorConstants.CHARACTER_DATA_TYPE)).thenReturn(
				mockDataTypeValidator);

		final ObservationValidator observationValidator =
				new ObservationValidator(mockVariableService, mockBmsRequestAttributes, mockObservationValidationDataExtractor,
						mockMeasurementDataValidatoyFactory);
		final Errors mockErrors = Mockito.mock(Errors.class);
		observationValidator.validate(mockObservation, mockErrors);

		Mockito.verify(mockDataTypeValidator, Mockito.times(1)).validateValues(measurementVariableDetails, 0, 0, mockErrors);

	}
}
