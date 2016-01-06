
package org.ibp.api.domain.study.validators;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ibp.api.domain.common.BmsRequestAttributeImpl;
import org.ibp.api.domain.common.BmsRequestAttributes;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

@Component
public class ObservationValidator implements Validator {

	@Autowired
	private VariableService variableService;

	@Autowired
	private StudyService studyService;

	ObservationValidationDataExtractor observationValidationDataExtractor;

	final BmsRequestAttributes bmsRequestAttributes;

	private final MeasurementDataValidatorFactory measurementDataValidatorFactory;

	ObservationValidator() {
		this.bmsRequestAttributes = new BmsRequestAttributeImpl();
		this.measurementDataValidatorFactory = new MeasurementDataValidatorFactoryImpl();
	}

	/**
	 * This need to be post construct because the variable service and study service are not available on object creation.
	 * @throws Exception
	 */
	@PostConstruct
	public void initIt() throws Exception {
		this.observationValidationDataExtractor = new ObservationValidationDataExtractor(variableService, studyService);
	}

	public ObservationValidator(final VariableService variableService, final BmsRequestAttributes bmsRequestAttributes,
			final ObservationValidationDataExtractor observationValidationDataExtractor,
			final MeasurementDataValidatorFactory measurementDataValidatoyFactory) {
		this.variableService = variableService;
		this.bmsRequestAttributes = bmsRequestAttributes;
		this.observationValidationDataExtractor = observationValidationDataExtractor;
		this.measurementDataValidatorFactory = measurementDataValidatoyFactory;

	}

	@Override
	public boolean supports(final Class<?> clazz) {
		return Observation.class.equals(clazz);
	}

	@Override
	public void validate(final Object target, final Errors errors) {

		final Observation observation = (Observation) target;
		final int errorCountBeforeNullValidation = errors.getErrorCount();
		validateInputData(observation, errors);
		final int errorCountAfterNullValidation = errors.getErrorCount();

		List<ObjectError> allErrors = errors.getAllErrors();
		if(errorCountAfterNullValidation - errorCountBeforeNullValidation > 0) {
			// No point since we have null values;
			return;
		}
		final ObservationValidationData observationValidationData =
				this.observationValidationDataExtractor.getObservationValidationData(observation,
						this.bmsRequestAttributes.getRequestAttributes());
		final Map<Integer, MeasurementVariableDetails> measurementVariableDetailsList =
				observationValidationData.getMeasurementVariableDetailsList();
		final Set<Entry<Integer, MeasurementVariableDetails>> measurmentVariableDetails = measurementVariableDetailsList.entrySet();

		for (final Entry<Integer, MeasurementVariableDetails> measurementEntry : measurmentVariableDetails) {
			final MeasurementVariableDetails measurementVariableDetails = measurementEntry.getValue();
			final DataType measurementVariableDataType = measurementVariableDetails.getVariableDataType();
			final DataTypeValidator measurementValidator =
					this.measurementDataValidatorFactory.getMeasurementValidator(measurementVariableDataType);
			errors.pushNestedPath("measurement[" + measurementEntry.getKey() + "]");
			measurementValidator.validateValues(measurementVariableDetails, measurementEntry.getKey(),
					observation.getUniqueIdentifier(), errors);
			errors.popNestedPath();
		}
	}

	void validateInputData(final Observation observation, Errors errors) {

		if (observation.getUniqueIdentifier() == null) {
			errors.rejectValue("uniqueIdentifier", "invalid.value.null");
		}

		final List<Measurement> measurements = observation.getMeasurements();
		if (measurements == null || observation.getMeasurements().isEmpty()) {
			errors.rejectValue("measurements", "invalid.value.null");
			return;
		}

		int measurementCounter = 0;
		for (Measurement measurement : measurements) {
			validateMeasurementValue(errors, measurementCounter, measurement);
			validateMeasurementIdentifier(errors, measurementCounter, measurement);
			measurementCounter++;
		}
	}

	private void validateMeasurementIdentifier(Errors errors, int measurementCounter, Measurement measurement) {
		final MeasurementIdentifier measurementIdentifier = measurement.getMeasurementIdentifier();
		if (measurementIdentifier == null) {
			errors.rejectValue("measurements[" + measurementCounter + "].measurementIdentifier", "invalid.value.null");
		} else {
			validateTrait(errors, measurementCounter, measurementIdentifier);
		}
	}

	private void validateTrait(Errors errors, int measurementCounter, final MeasurementIdentifier measurementIdentifier) {
		final Trait trait = measurementIdentifier.getTrait();
		if (trait == null) {
			errors.rejectValue("measurements[" + measurementCounter + "].trait", "invalid.value.null");
		} else {
			if (trait.getTraitId() == null) {
				errors.rejectValue("measurements[" + measurementCounter + "].trait.traitId", "invalid.value.null");
			}

			if (StringUtils.isEmpty(trait.getTraitName())) {
				errors.rejectValue("measurements[" + measurementCounter + "].trait.traitName", "invalid.value.null");
			}
		}
	}

	private void validateMeasurementValue(Errors errors, int measurementCounter, Measurement measurement) {
		if (StringUtils.isEmpty(measurement.getMeasurementValue())) {
			errors.rejectValue("measurements[" + measurementCounter + "].measurementValue", "invalid.value.null");
		}
	}

}
