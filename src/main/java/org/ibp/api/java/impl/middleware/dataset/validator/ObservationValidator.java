package org.ibp.api.java.impl.middleware.dataset.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component("datasetObservationValidator")
public class ObservationValidator {
	
	@Autowired
	private DatasetService datasetService;

	@Autowired
	protected OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private StudyDataManager studyDataManager;

	private BindingResult errors;
	
	public void validateObservationUnit(final Integer datasetId, final Integer observationUnitId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		
		final boolean isValid = this.datasetService.isValidObservationUnit(datasetId, observationUnitId);
		if (!isValid) {
			this.errors.reject("invalid.observation.unit.id", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

	public void validateObservation(
		final Integer studyId, final Integer datasetId, final Integer observationUnitId,
		final Integer observationId, final ObservationDto observationDto) {

		this.validateObservationUnit(datasetId, observationUnitId);

		final Phenotype phenotype = this.datasetService.getPhenotype(observationUnitId, observationId);
		if (phenotype == null) {

			this.errors.reject("invalid.observation.id", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		if (observationDto != null) {
			this.validateObservationValue(studyId, phenotype.getObservableId(), observationDto.getValue());
			if (observationDto.getDraftValue() != null) {
				this.validateObservationValue(studyId, phenotype.getObservableId(), observationDto.getDraftValue());
			}
		}
	}

	public void validateObservationValue(final Integer studyId, final Integer variableId, final String value) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final String programUuid = this.studyDataManager.getStudy(studyId).getProgramUUID();
		final Variable var = this.ontologyVariableDataManager.getVariable(programUuid, variableId, true);

		if (!isValidValue(var, value)) {
			this.errors.reject("invalid.observation.value");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	// TODO merge with ObservationsTableValidator.isValidValue
	private static boolean isValidValue(final Variable var, final String value) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (var.getMinValue() != null && var.getMaxValue() != null) {
			return validateIfValueIsMissingOrNumber(value.trim());
		} else if (var.getScale().getDataType() == DataType.NUMERIC_VARIABLE) {
			return validateIfValueIsMissingOrNumber(value.trim());
		} else if (var.getScale().getDataType() == DataType.DATE_TIME_VARIABLE) {
			return new DateValidator().isValid(value, "yyyyMMdd");
		}
		return true;
	}

	private static boolean validateIfValueIsMissingOrNumber(final String value) {
		if (MeasurementData.MISSING_VALUE.equals(value.trim())) {
			return true;
		}
		return NumberUtils.isNumber(value);
	}
}
