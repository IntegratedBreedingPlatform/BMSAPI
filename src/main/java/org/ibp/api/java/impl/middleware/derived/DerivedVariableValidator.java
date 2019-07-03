package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.derived_variables.DerivedVariableService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class DerivedVariableValidator {

	public static final String STUDY_EXECUTE_CALCULATION_INVALID_REQUEST = "study.execute.calculation.invalid.request";
	public static final String STUDY_EXECUTE_CALCULATION_FORMULA_NOT_FOUND = "study.execute.calculation.formula.not.found";
	public static final String STUDY_EXECUTE_CALCULATION_MISSING_VARIABLES = "study.execute.calculation.missing.variables";
	public static final String STUDY_EXECUTE_CALCULATION_NOT_AGGREGATE_FUNCTION = "study.execute.calculation.not.aggregate.function";

	@Resource
	private FormulaService formulaService;

	@Resource
	private DatasetService datasetService;

	@Resource
	private DerivedVariableService middlewareDerivedVariableService;

	private static String AGGREGATE_FUNCTIONS = "(AVG|COUNT|DISTINCT_COUNT|MAX|MIN|SUM)";

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

			Integer plotDatasetId = this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))).get(0).getDatasetId();
			final Set<Integer> variableIds;
			if(plotDatasetId.equals(datasetId)) {
				// Get all possible input variables from Environment Detail, Study Condition, and Plot/Subobs Traits
				variableIds = this.middlewareDerivedVariableService.createVariableIdMeasurementVariableMap(studyId).keySet();
			} else {
				// Get all possible input variables from Subobs Traits
				variableIds = this.getVariableIdsOfTraitsInDataset(datasetId);
			}

			final Set<String> inputMissingVariables = new HashSet<>();
			for (final FormulaVariable formulaVariable : formulaOptional.get().getInputs()) {
				if (!variableIds.contains(formulaVariable.getId())) {
					inputMissingVariables.add(formulaVariable.getName());
				}
			}
			if (!inputMissingVariables.isEmpty()) {
				errors.reject(
					STUDY_EXECUTE_CALCULATION_MISSING_VARIABLES,
					new String[] {StringUtils.join(inputMissingVariables.toArray(), ", ")}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

		}

	}

	public void verifySubObservationsInputVariablesInAggregateFunction(final int variableId, final int studyId, final int datasetId, final Map<Integer, Integer> inputVariableDatasetMap) {
		Integer plotDatasetId = this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))).get(0).getDatasetId();
		if(!plotDatasetId.equals(datasetId)) {
			return;
		}

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		if (formulaOptional.isPresent()) {
			final List<DatasetDTO> subobsDatasets = this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId(), DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId(), DatasetTypeEnum.TIME_SERIES_SUBOBSERVATIONS.getId(), DatasetTypeEnum.CUSTOM_SUBOBSERVATIONS.getId())));
			final List<Integer> subobservationIds = new ArrayList<>();
			for(DatasetDTO dataset: subobsDatasets) {
				subobservationIds.add(dataset.getDatasetId());
			}
			for (final FormulaVariable formulaVariable : formulaOptional.get().getInputs()) {
				if(subobservationIds.contains(inputVariableDatasetMap.get(formulaVariable.getId()))) {
					validateSubobservationInputVariable(formulaOptional, formulaVariable);
				}
			}
		}
	}

	void validateSubobservationInputVariable(final Optional<FormulaDto> formulaOptional, final FormulaVariable formulaVariable) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Pattern pattern = Pattern.compile("^.*(?i)" + AGGREGATE_FUNCTIONS + "\\(\\{\\{" + formulaVariable.getId() + "}}\\).*$");
		if(!pattern.matcher(formulaOptional.get().getDefinition()).matches()) {
			errors.reject(STUDY_EXECUTE_CALCULATION_NOT_AGGREGATE_FUNCTION);
			throw new ApiRequestValidationException(errors.getAllErrors());
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
