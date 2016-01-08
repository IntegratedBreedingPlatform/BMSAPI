
package org.ibp.api.domain.study.validators;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.ibp.api.domain.common.BmsRequestAttributes;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.google.common.collect.Lists;

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
						Matchers.<Map<?, ?>>anyObject())).thenReturn(mockObservationValidationData);

		final MeasurementDetails measurementVariableDetails =
				new MeasurementDetails(TestValidatorConstants.TEST_MEASUREMENT_VARIABLE_ID,
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

	@Test
	public void testOnBasicErrorsWePerformNoFutherValidation() throws Exception {

		final ObservationValidationDataExtractor mockObservationValidationDataExtractor =
				Mockito.mock(ObservationValidationDataExtractor.class);

		final ObservationValidator observationValidator =
				new ObservationValidator(null, null, mockObservationValidationDataExtractor,
						null);
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Test");

		observationValidator.validate(new Observation(), errors);

		// The getObservationValidationData should never get called
		Mockito.verify(mockObservationValidationDataExtractor, Mockito.times(0)).getObservationValidationData(Matchers.<Observation>anyObject(),
				Matchers.<Map<?, ?>>anyObject());

	}

	@Test
	public void testValidateInputData() throws Exception {
		final ObservationValidator observationValidator =
				new ObservationValidator();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Test");
		observationValidator.validateInputData(new Observation(), errors);
		Assert.assertThat(errors.getAllErrors().size(),CoreMatchers.equalTo(2));
		assertFieldError((FieldError) errors.getAllErrors().get(0), "invalid.value.null", "uniqueIdentifier");
		assertFieldError((FieldError) errors.getAllErrors().get(1), "invalid.value.null", "measurements");
	}

	@Test
	public void testValidateInputDataWithMissingMeasurementIdentifierAndValue() throws Exception {
		final ObservationValidator observationValidator =
				new ObservationValidator();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Test");
		Observation observation = new Observation();
		observation.setUniqueIdentifier(1233);
		observation.setMeasurements(Collections.singletonList(new Measurement()));
		observationValidator.validateInputData(observation, errors);
		Assert.assertThat(errors.getAllErrors().size(),CoreMatchers.equalTo(2));

		assertFieldError((FieldError) errors.getAllErrors().get(0), "invalid.value.null", "measurements[0].measurementValue");
		assertFieldError((FieldError) errors.getAllErrors().get(1), "invalid.value.null", "measurements[0].measurementIdentifier");

	}

	@Test
	public void testValidateInputDataWithMissingTrait() throws Exception {
		final ObservationValidator observationValidator =
				new ObservationValidator();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Test");
		Observation observation = new Observation();
		observation.setUniqueIdentifier(1233);
		Measurement measurement = new Measurement();
		measurement.setMeasurementValue("test");
		MeasurementIdentifier measurementIdentifier = new MeasurementIdentifier();
		measurement.setMeasurementIdentifier(measurementIdentifier);
		observation.setMeasurements(Collections.singletonList(measurement));
		observationValidator.validateInputData(observation, errors);
		Assert.assertThat(errors.getAllErrors().size(),CoreMatchers.equalTo(1));
		assertFieldError((FieldError) errors.getAllErrors().get(0), "invalid.value.null", "measurements[0].trait");
	}

	@Test
	public void testValidateInputDataWithMissingTraitNameAndValue() throws Exception {
		final ObservationValidator observationValidator =
				new ObservationValidator();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Test");
		Observation observation = new Observation();
		observation.setUniqueIdentifier(1233);
		Measurement measurement = new Measurement();
		measurement.setMeasurementValue("test");
		MeasurementIdentifier measurementIdentifier = new MeasurementIdentifier();
		measurement.setMeasurementIdentifier(measurementIdentifier);
		observation.setMeasurements(Collections.singletonList(measurement));
		measurementIdentifier.setTrait(new Trait());
		observationValidator.validateInputData(observation, errors);
		Assert.assertThat(errors.getAllErrors().size(),CoreMatchers.equalTo(2));
		assertFieldError((FieldError) errors.getAllErrors().get(0), "invalid.value.null", "measurements[0].trait.traitId");
		assertFieldError((FieldError) errors.getAllErrors().get(1), "invalid.value.null", "measurements[0].trait.traitName");

	}

	private void assertFieldError(FieldError uniqueIdentifierErrorMessage, final String expectedCode, final String field) {
		Assert.assertThat(uniqueIdentifierErrorMessage.getCode(), CoreMatchers.equalTo(expectedCode));
		Assert.assertThat(uniqueIdentifierErrorMessage.getField(), CoreMatchers.equalTo(field));
	}

	@Test
	public void testSupports() throws Exception {
		final ObservationValidator observationValidator =
				new ObservationValidator();
		Assert.assertThat(observationValidator.supports(Observation.class), CoreMatchers.equalTo(true));
	}
}
