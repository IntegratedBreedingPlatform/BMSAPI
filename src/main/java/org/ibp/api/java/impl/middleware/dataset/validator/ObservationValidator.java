package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.util.VariableValueUtil;
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

	private BindingResult errors;

	public void validateObservationUnit(final Integer datasetId, final Integer observationUnitId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final boolean isValid = this.datasetService.isValidObservationUnit(datasetId, observationUnitId);
		if (!isValid) {
			this.errors.reject("invalid.observation.unit.id", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

	public void validateObservation(final Integer datasetId, final Integer observationUnitId,
		final Integer observationId, final ObservationDto observationDto) {

		this.validateObservationUnit(datasetId, observationUnitId);

		final Phenotype phenotype = this.datasetService.getPhenotype(observationUnitId, observationId);
		if (phenotype == null) {

			this.errors.reject("invalid.observation.id", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		if (observationDto != null) {
			this.validateVariableValue(phenotype.getObservableId(), observationDto.getValue());
			if (observationDto.getDraftValue() != null) {
				this.validateVariableValue(phenotype.getObservableId(), observationDto.getDraftValue());
			}
		}
	}

	public void validateVariableValue(final Integer variableId, final String value) {

		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final Variable var = this.ontologyVariableDataManager.getVariable(ContextHolder.getCurrentProgram(), variableId, true);

		if (!VariableValueUtil.isValidObservationValue(var, value)) {
			this.errors.reject("invalid.variable.value");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
