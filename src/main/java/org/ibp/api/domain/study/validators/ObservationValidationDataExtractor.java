
package org.ibp.api.domain.study.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.ontology.VariableService;

import com.google.common.base.Preconditions;

public class ObservationValidationDataExtractor {

	private static final String CANNOT_BE_NULL = "cannot be null. ";
	private static final String PLEASE_CONTACT_ADMINISTRATOR_FOR_FURTHER_ASSISTANCE =
			"Please contact administrator for further assistance.";

	ObservationValidationDataExtractor() {

	}

	ObservationValidationData getObservationValidationData(final Observation observation, final Map<?, ?> requestAttributes,
			final VariableService variableService) {
		Preconditions.checkNotNull(observation, "Observation parameter cannot be null");
		Preconditions.checkNotNull(requestAttributes, "Request attributes parameter cannot be null");
		Preconditions.checkNotNull(variableService, "Variable Service parameter cannot be null");

		List<Measurement> measurements = observation.getMeasurements();
		if (measurements == null) {
			measurements = new ArrayList<>();
		}

		final int measurementListSize = measurements.size();

		final Map<Integer, MeasurementVariableDetails> measurementVariableDetailsList = new HashMap<>();
		for (int counter = 0; counter < measurementListSize; counter++) {
			final VariableDetails variableDetails = this.getVariableDetails(measurements, counter, requestAttributes, variableService);
			final Measurement measurement = measurements.get(counter);
			measurementVariableDetailsList.put(counter, new MeasurementVariableDetails(variableDetails.getId(), variableDetails.getName(),
					this.getVariableDataType(variableDetails), this.getVariableValidValues(variableDetails), measurement
							.getMeasurementIdentifier().getMeasurementId(), measurement.getMeasurementValue()));
		}

		return new ObservationValidationData(this.getCropName(requestAttributes), this.getProgramId(requestAttributes),
				this.getStudyId(requestAttributes), this.getObservationId(observation), measurementVariableDetailsList);

	}

	Integer getObservationId(final Observation observation) {
		final Integer observationId = observation.getUniqueIdentifier();
		Preconditions.checkNotNull(observationId, "ObservationId " + ObservationValidationDataExtractor.CANNOT_BE_NULL
				+ ObservationValidationDataExtractor.PLEASE_CONTACT_ADMINISTRATOR_FOR_FURTHER_ASSISTANCE);
		Preconditions.checkState(observationId != 0, "The observation identifier cannot be 0. "
				+ ObservationValidationDataExtractor.PLEASE_CONTACT_ADMINISTRATOR_FOR_FURTHER_ASSISTANCE);
		return observationId;
	}

	String getCropName(final Map<?, ?> requestAttributes) {
		return this.getRequestAttributeValue("cropname", "Cropname " + ObservationValidationDataExtractor.CANNOT_BE_NULL
				+ ObservationValidationDataExtractor.PLEASE_CONTACT_ADMINISTRATOR_FOR_FURTHER_ASSISTANCE, requestAttributes);
	}

	String getProgramId(final Map<?, ?> requestAttributes) {
		return this.getRequestAttributeValue("programId", "ProgramId " + ObservationValidationDataExtractor.CANNOT_BE_NULL
				+ ObservationValidationDataExtractor.PLEASE_CONTACT_ADMINISTRATOR_FOR_FURTHER_ASSISTANCE, requestAttributes);
	}

	String getStudyId(final Map<?, ?> requestAttributes) {
		return this.getRequestAttributeValue("studyId", "ProgramId " + ObservationValidationDataExtractor.CANNOT_BE_NULL
				+ ObservationValidationDataExtractor.PLEASE_CONTACT_ADMINISTRATOR_FOR_FURTHER_ASSISTANCE, requestAttributes);
	}

	VariableDetails getVariableDetails(final List<Measurement> measurements, final int measurementIndex, final Map<?, ?> requestAttributes,
			final VariableService variableService) {

		final Measurement measurement = measurements.get(measurementIndex);

		// No null checking here as we will be validating non null via annotations.
		final MeasurementIdentifier measurementIdentifier = measurement.getMeasurementIdentifier();
		final Trait trait = measurementIdentifier.getTrait();
		final Integer traitId = trait.getTraitId();
		return variableService.getVariableById(this.getCropName(requestAttributes), this.getProgramId(requestAttributes),
				traitId.toString());
	}

	DataType getVariableDataType(final VariableDetails variableDetails) {
		final ScaleDetails scale = variableDetails.getScale();
		Preconditions.checkNotNull(scale, "Scale should never be null. Please contact adimistrator for further assistance");
		return scale.getDataType();
	}

	ValidValues getVariableValidValues(final VariableDetails variableDetails) {
		final ScaleDetails scale = variableDetails.getScale();
		Preconditions.checkNotNull(scale, "Scale should never be null. Please contact adimistrator for further assistance");
		return scale.getValidValues();
	}

	private String getRequestAttributeValue(final String attributeName, final String errorMessage, final Map<?, ?> requestAttributes) {
		final String returnValue = (String) requestAttributes.get(attributeName);
		Preconditions.checkNotNull(returnValue, errorMessage);
		return returnValue;
	}
}
