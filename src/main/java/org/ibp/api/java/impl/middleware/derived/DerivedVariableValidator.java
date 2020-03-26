package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.dms.DatasetBasicDTO;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.derived_variables.DerivedVariableService;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DerivedVariableValidator {
	static final String STUDY_EXECUTE_CALCULATION_INVALID_REQUEST = "study.execute.calculation.invalid.request";
	static final String STUDY_EXECUTE_CALCULATION_FORMULA_NOT_FOUND = "study.execute.calculation.formula.not.found";
	static final String STUDY_EXECUTE_CALCULATION_MISSING_VARIABLES = "study.execute.calculation.missing.variables";
	static final String STUDY_EXECUTE_CALCULATION_NOT_AGGREGATE_FUNCTION = "study.execute.calculation.not.aggregate.function";
	static final String STUDY_EXECUTE_CALCULATION_INPUT_NOT_IN_SUBLEVEL = "study.execute.calculation.input.not.in.sublevel";

	@Resource
	private FormulaService formulaService;

	@Resource
	private DatasetTypeService datasetTypeService;

	@Resource
	private DerivedVariableService middlewareDerivedVariableService;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	public void validate(final Integer variableId, final List<Integer> geoLocationIds) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (geoLocationIds == null || variableId == null) {
			errors.reject(STUDY_EXECUTE_CALCULATION_INVALID_REQUEST);
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		if (!formulaOptional.isPresent()) {
			errors.reject(STUDY_EXECUTE_CALCULATION_FORMULA_NOT_FOUND);
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	public void verifyInputVariablesArePresentInStudy(final Integer variableId, final Integer datasetId, final Integer studyId) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		if (formulaOptional.isPresent()) {

			final Set<FormulaVariable> missingFormulaVariablesInStudy =
				this.middlewareDerivedVariableService.getMissingFormulaVariablesInStudy(studyId, datasetId, variableId);
			final Set<String> inputMissingVariables = new HashSet<>();
			for (final FormulaVariable formulaVariable : missingFormulaVariablesInStudy) {
				inputMissingVariables.add(formulaVariable.getName());
			}
			if (!inputMissingVariables.isEmpty()) {
				errors.reject(
					STUDY_EXECUTE_CALCULATION_MISSING_VARIABLES,
					new String[] {StringUtils.join(inputMissingVariables.toArray(), ", ")}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

		}

	}

	void validateForAggregateFunctions(final int variableId, final int studyId, final int datasetId,
		final Map<Integer, Integer> inputVariableDatasetMap) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		if (formulaOptional.isPresent()) {
			final List<String> aggregateInputVariables = DerivedVariableUtils.getAggregateFunctionInputVariables(formulaOptional.get().getDefinition(), false);
			final List<DatasetBasicDTO> subobsDatasets =
				this.middlewareDatasetService.getDatasetBasicDTOs(studyId, new HashSet<>(this.datasetTypeService.getSubObservationDatasetTypeIds()));
			final List<Integer> subobservationIds = subobsDatasets.stream().map(DatasetBasicDTO::getDatasetId).collect(Collectors.toList());
			final Integer plotDatasetId =
				this.middlewareDatasetService.getDatasetBasicDTOs(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))).get(0)
					.getDatasetId();
			if(!aggregateInputVariables.isEmpty()) {
				if (!plotDatasetId.equals(datasetId)) {
					errors.reject(STUDY_EXECUTE_CALCULATION_INPUT_NOT_IN_SUBLEVEL);
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
				this.verifyAggregateInputVariablesInSubObsLevel(subobservationIds, inputVariableDatasetMap, aggregateInputVariables);
			}
			if (plotDatasetId.equals(datasetId)) {
				this.verifySubObservationsInputVariablesInAggregateFunction(
					subobservationIds, inputVariableDatasetMap, formulaOptional, aggregateInputVariables);
			}
		}
	}

	void verifySubObservationsInputVariablesInAggregateFunction(final List<Integer> subobservationIds, final Map<Integer, Integer> inputVariableDatasetMap, final Optional<FormulaDto> formulaOptional,
		final List<String> aggregateInputVariables) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		for (final FormulaVariable formulaVariable : formulaOptional.get().getInputs()) {
			if (subobservationIds.contains(inputVariableDatasetMap.get(formulaVariable.getId())) && !aggregateInputVariables.contains(String.valueOf(formulaVariable.getId()))) {
				errors.reject(STUDY_EXECUTE_CALCULATION_NOT_AGGREGATE_FUNCTION);
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

	void verifyAggregateInputVariablesInSubObsLevel(final List<Integer> subobservationIds, final Map<Integer, Integer> inputVariableDatasetMap, final List<String> aggregateInputVariables) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		for(final String aggregateInputVariable: aggregateInputVariables) {
			if(!subobservationIds.contains(inputVariableDatasetMap.get(Integer.valueOf(aggregateInputVariable)))) {
				errors.reject(STUDY_EXECUTE_CALCULATION_INPUT_NOT_IN_SUBLEVEL);
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}
}
