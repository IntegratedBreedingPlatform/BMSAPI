package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.dataset.DatasetService;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DerivedVariableValidator {

	private BindingResult errors;

	@Resource
	private FormulaService formulaService;

	@Resource
	private DatasetService datasetService;

	public void validate(final Integer variableId, final List<Integer> geoLocationIds) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (geoLocationIds == null || variableId == null) {
			errors.reject("study.execute.calculation.invalid.request");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		if (!formulaOptional.isPresent()) {
			errors.reject("study.execute.calculation.formula.not.found");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void verifyMissingInputVariables(final Integer variableId, final Integer datasetId) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		if (formulaOptional.isPresent()) {

			// Verify that variables are present
			final Set<Integer> variableIdsOfTraitsInStudy = getVariableIdsOfTraitsInDataset(datasetId);
			final Set<String> inputMissingVariables = new HashSet<>();
			for (final FormulaVariable formulaVariable : formulaOptional.get().getInputs()) {
				if (!variableIdsOfTraitsInStudy.contains(formulaVariable.getId())) {
					inputMissingVariables.add(formulaVariable.getName());
				}
			}
			if (!inputMissingVariables.isEmpty()) {
				errors.reject("study.execute.calculation.missing.variables",
					new String[] {StringUtils.join(inputMissingVariables.toArray(), ", ")}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

		}

	}

	protected Set<Integer> getVariableIdsOfTraitsInDataset(final int datasetId) {

		final Set<Integer> variableIdsOfTraitsInDataset = new HashSet<>();
		final List<MeasurementVariable> traits =
			datasetService.getMeasurementVariables(datasetId, Arrays.asList(VariableType.TRAIT.getId()));

		if (!traits.isEmpty()) {
			for (final MeasurementVariable trait : traits) {
				variableIdsOfTraitsInDataset.add(trait.getTermId());
			}
		}

		return variableIdsOfTraitsInDataset;

	}

}
