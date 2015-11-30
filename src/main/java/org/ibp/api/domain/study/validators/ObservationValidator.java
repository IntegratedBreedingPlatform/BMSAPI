
package org.ibp.api.domain.study.validators;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ibp.api.domain.common.BmsRequestAttributeImpl;
import org.ibp.api.domain.common.BmsRequestAttributes;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ObservationValidator implements Validator {

	@Autowired
	private VariableService variableService;

	final ObservationValidationDataExtractor observationValidationDataExtractor;

	final BmsRequestAttributes bmsRequestAttributes;

	private final MeasurementDataValidatorFactory measurementDataValidatorFactory;

	ObservationValidator() {
		this.observationValidationDataExtractor = new ObservationValidationDataExtractor();
		this.bmsRequestAttributes = new BmsRequestAttributeImpl();
		this.measurementDataValidatorFactory = new MeasurementDataValidatorFactoryImpl();
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
		final ObservationValidationData observationValidationData =
				this.observationValidationDataExtractor.getObservationValidationData(observation,
						this.bmsRequestAttributes.getRequestAttributes(), this.variableService);
		final Map<Integer, MeasurementVariableDetails> measurementVariableDetailsList =
				observationValidationData.getMeasurementVariableDetailsList();
		final Set<Entry<Integer, MeasurementVariableDetails>> measurmentVariableDetails = measurementVariableDetailsList.entrySet();

		for (final Entry<Integer, MeasurementVariableDetails> measurementEntry : measurmentVariableDetails) {
			final MeasurementVariableDetails measurementVariableDetails = measurementEntry.getValue();
			final DataType measurementVariableDataType = measurementVariableDetails.getVariableDataType();
			final DataTypeValidator measurementValidator =
					this.measurementDataValidatorFactory.getMeasurementValidator(measurementVariableDataType);
			measurementValidator.validateValues(measurementVariableDetails, measurementEntry.getKey(),
					observation.getUniqueIdentifier(), errors);
		}
	}

}
