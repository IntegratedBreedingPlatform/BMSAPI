package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.fest.util.Collections;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.dms.VariableDatasetsDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.derived.DerivedVariableService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ibp.api.java.impl.middleware.dataset.DatasetServiceImpl.MISSING_VALUE;

@Service
@Transactional
public class DerivedVariableServiceImpl implements DerivedVariableService {

	private static final Logger LOG = LoggerFactory.getLogger(DerivedVariableServiceImpl.class);
	public static final String HAS_DATA_OVERWRITE_RESULT_KEY = "hasDataOverwrite";
	static final String INPUT_MISSING_DATA_RESULT_KEY = "inputMissingData";
	static final String INPUT_DATA_ARITHMETIC_WARNING_KEY = "inputDataArithmeticWarning";
	public static final String STUDY_EXECUTE_CALCULATION_PARSING_EXCEPTION = "study.execute.calculation.parsing.exception";
	static final String STUDY_EXECUTE_CALCULATION_ENGINE_EXCEPTION = "study.execute.calculation.engine.exception";
	static final String STUDY_EXECUTE_CALCULATION_MISSING_DATA = "study.execute.calculation.missing.data";
	static final String STUDY_EXECUTE_CALCULATION_ARITHMETIC_EXCEPTION = "study.execute.calculation.arithmetic.exception";
	private static final String STUDY_EXECUTE_CALCULATION_HAS_EXISTING_DATA = "study.execute.calculation.has.existing.data";

	@Resource
	private DatasetService middlewareDatasetService;

	@Resource
	private org.generationcp.middleware.service.api.derived_variables.DerivedVariableService middlewareDerivedVariableService;

	@Resource
	private DatasetValidator datasetValidator;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private DerivedVariableValidator derivedVariableValidator;

	@Resource
	private DerivedVariableProcessor processor;

	@Resource
	private FormulaService formulaService;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Resource
	private DatasetTypeService datasetTypeService;

	public DerivedVariableServiceImpl() {
		// do nothing
	}

	@Override
	public Map<String, Object> execute(
		final int studyId, final int datasetId, final Integer variableId, final List<Integer> geoLocationIds,
		final Map<Integer, Integer> inputVariableDatasetMap,
		final boolean overwriteExistingData) {

		final Map<String, Object> results = new HashMap<>();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.derivedVariableValidator.validate(variableId, geoLocationIds);
		this.derivedVariableValidator.verifyInputVariablesArePresentInStudy(variableId, datasetId, studyId);
		this.derivedVariableValidator
			.validateForAggregateFunctions(variableId, studyId, datasetId, inputVariableDatasetMap);

		// Get the list of observation unit rows grouped by intances
		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap =
			this.middlewareDatasetService.getInstanceIdToObservationUnitRowsMap(studyId, datasetId, geoLocationIds);
		// Get the the measurement variables of all environment details, environment conditions and traits in a study so
		// that we can determine the datatype and possibleValue of a ObservationUnitData and input variable values from different
		// levels.
		final Map<Integer, MeasurementVariable> measurementVariablesMap =
			this.middlewareDerivedVariableService.createVariableIdMeasurementVariableMapInStudy(studyId);

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		final FormulaDto formula = formulaOptional.get();
		final MeasurementVariable targetMeasurementVariable = measurementVariablesMap.get(formula.getTarget().getId());
		final Map<String, Object> parameters = DerivedVariableUtils.extractParameters(formula.getDefinition());
		final List<String> aggregateInputVariables = DerivedVariableUtils.getAggregateFunctionInputVariables(formula.getDefinition(), true);

		// Get the list of input variables that should be read from environment level.
		final List<String> environmentInputVariables = this.getInputVariablesFromSummary(studyId, inputVariableDatasetMap);

		// Calculate
		final Set<String> inputMissingData = new HashSet<>();

		// Retrieve TRAIT input variables' data from sub-observation level. Aggregate values are grouped by plot observation's experimentId
		final Map<Integer, Map<String, List<Object>>> valuesFromSubObservation =
			this.middlewareDerivedVariableService
				.getValuesFromObservations(studyId, this.datasetTypeService.getSubObservationDatasetTypeIds(),
					inputVariableDatasetMap);

		final Set<Integer> observationUnitIds = new HashSet<>();
		// Iterate through the observations for each instances
		for (final List<ObservationUnitRow> observations : instanceIdObservationUnitRowsMap.values()) {
			for (final ObservationUnitRow observation : observations) {

				// Get input data
				final Map<String, Object> rowParameters = new HashMap<>(parameters);
				final Set<String> rowInputMissingData = new HashSet<>();

				try {
					// Fill parameters with input variable values from the current level. Environment Detail and Study Condition variable
					// values from environment level are already included in ObservationUnitRow.
					rowInputMissingData.addAll(
						DerivedVariableUtils.extractValues(rowParameters, observation, measurementVariablesMap, aggregateInputVariables,
							environmentInputVariables));
				} catch (final ParseException e) {
					LOG.error("Error parsing date value for parameters " + rowParameters, e);
					errors.reject(STUDY_EXECUTE_CALCULATION_PARSING_EXCEPTION);
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
				inputMissingData.addAll(rowInputMissingData);

				try {
					// Set the aggregate values from subobservation level to the processor
					this.fillWithSubObservationLevelValues(observation.getObservationUnitId(), valuesFromSubObservation,
						measurementVariablesMap,
						rowInputMissingData, rowParameters, aggregateInputVariables);
				} catch (final ParseException e) {
					LOG.error("Error parsing date value for parameters " + rowParameters, e);
					errors.reject(STUDY_EXECUTE_CALCULATION_PARSING_EXCEPTION);
					throw new ApiRequestValidationException(errors.getAllErrors());
				}

				if (!rowInputMissingData.isEmpty() || rowParameters.values().contains("")) {
					continue;
				}

				// Evaluate
				String value;
				try {
					final String executableFormula = DerivedVariableUtils.replaceDelimiters(formula.getDefinition());
					value = this.processor.evaluateFormula(executableFormula, rowParameters);
				} catch (final Exception e) {
					if (e.getCause() instanceof ArithmeticException) {
						// If formula cannot be evaluated due to arithmetic error (e.g. dividing number by zero), ignore the error and set the value as missing.
						LOG.error("Error evaluating formula due to arithmetic error " + formula + " with inputs " + rowParameters, e);
						value = MISSING_VALUE;
						// Just warn the user that there are some input data that cannot be evaluated due to arithmetic error.
						results.put(
							INPUT_DATA_ARITHMETIC_WARNING_KEY, this.resourceBundleMessageSource
								.getMessage(STUDY_EXECUTE_CALCULATION_ARITHMETIC_EXCEPTION,
									new String[] {},
									Locale.getDefault()));
					} else {
						LOG.error("Error evaluating formula " + formula + " with inputs " + rowParameters, e);
						errors.reject(STUDY_EXECUTE_CALCULATION_ENGINE_EXCEPTION);
						throw new ApiRequestValidationException(errors.getAllErrors());
					}
				}

				if (StringUtils.isBlank(value)) {
					continue;
				}

				// Process calculation result
				final ObservationUnitData target = observation.getVariables().get(formula.getTarget().getName());

				// Check if the calculated value matches any of the possible categorical values and get its categorical id.
				Integer categoricalId = null;
				if (targetMeasurementVariable.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
					for (final ValueReference possibleValue : targetMeasurementVariable.getPossibleValues()) {
						if (value.equalsIgnoreCase(possibleValue.getName())) {
							categoricalId = possibleValue.getId();
							break;
						}
					}
				}

				this.middlewareDerivedVariableService.saveCalculatedResult(
					value, categoricalId, observation.getObservationUnitId(),
					target.getObservationId(),
					targetMeasurementVariable);
				observationUnitIds.add(observation.getObservationUnitId());

				if (StringUtils.isNotEmpty(target.getValue()) && !target.getValue().equals(value)) {
					if (!overwriteExistingData) {
						// If there is an existing measurement data and the user did not explicitly choose to overwrite it, then throw a runtime exception
						// to rollback transaction so to prevent saving of calculated value.
						errors.reject(STUDY_EXECUTE_CALCULATION_HAS_EXISTING_DATA);
						throw new OverwriteDataException(errors.getAllErrors());
					} else {
						// Else, just warn the user that there's data to overwrite.
						results.put(HAS_DATA_OVERWRITE_RESULT_KEY, true);
					}

				}

			}

		}

		// Update the dependent phenotypes as out of sync in batch
		this.middlewareDatasetService.updateDependentPhenotypesAsOutOfSync(targetMeasurementVariable.getTermId(), observationUnitIds);

		// Process response
		if (!inputMissingData.isEmpty()) {
			// warn the user that there's missing data from input variables.
			results.put(
				INPUT_MISSING_DATA_RESULT_KEY, this.resourceBundleMessageSource
					.getMessage(STUDY_EXECUTE_CALCULATION_MISSING_DATA, new String[] {StringUtils.join(inputMissingData.toArray())},
						Locale.getDefault()));
		}

		return results;

	}

	void fillWithSubObservationLevelValues(final int observationUnitId,
		final Map<Integer, Map<String, List<Object>>> valuesFromSubObservation,
		final Map<Integer, MeasurementVariable> measurementVariablesMap,
		final Set<String> rowInputMissingData, final Map<String, Object> parameters,
		final List<String> inputVariables) throws ParseException {

		final Map<String, List<Object>> variableAggregateValuesMap = new HashMap<>();
		final Map<String, List<Object>> valuesMap = valuesFromSubObservation.get(observationUnitId);

		if (valuesMap != null) {
			for (final Map.Entry<String, List<Object>> entry : valuesMap.entrySet()) {
				final Integer variableId = Integer.valueOf(entry.getKey());
				final MeasurementVariable measurementVariable = measurementVariablesMap.get(variableId);
				final String termKey = DerivedVariableUtils.wrapTerm(entry.getKey());
				if (inputVariables.contains(termKey)) {
					variableAggregateValuesMap
						.put(termKey, DerivedVariableUtils.parseValueList(entry.getValue(), measurementVariable, rowInputMissingData));
					// If the input variable is in sub-observation level, remove its key from parameters because
					// aggregate data from subobservation should be passed through processor.setData() not parameters.
					parameters.remove(termKey);
				}
			}
			//Add empty list as parameter for variables with no data
			for (final String inputVariable : inputVariables) {
				if (parameters.containsKey(inputVariable)) {
					parameters.remove(inputVariable);
					variableAggregateValuesMap.put(inputVariable, new ArrayList<>());
				}
			}
			this.processor.setData(variableAggregateValuesMap);
		}

	}

	@Override
	public Set<FormulaVariable> getMissingFormulaVariablesInStudy(final int studyId, final int datasetId, final int variableId) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		return this.middlewareDerivedVariableService.getMissingFormulaVariablesInStudy(studyId, datasetId, variableId);
	}

	@Override
	public Set<FormulaVariable> getFormulaVariablesInStudy(final int studyId, final int datasetId) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		return this.middlewareDerivedVariableService.getFormulaVariablesInStudy(studyId, datasetId);
	}

	@Override
	public long countCalculatedVariablesInDatasets(final int studyId, final Set<Integer> datasetIds) {
		this.studyValidator.validate(studyId, false);
		for (final int datasetId : datasetIds) {
			this.datasetValidator.validateDataset(studyId, datasetId);
		}
		return this.middlewareDerivedVariableService.countCalculatedVariablesInDatasets(datasetIds);
	}

	@Override
	public Map<Integer, VariableDatasetsDTO> getFormulaVariableDatasetsMap(final Integer studyId, final Integer datasetId,
		final Integer variableId) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		return this.middlewareDerivedVariableService.createVariableDatasetsMap(studyId, datasetId, variableId);
	}

	private List<String> getInputVariablesFromSummary(final int studyId, final Map<Integer, Integer> inputVariableDatasetMap) {
		final DatasetDTO summaryDataset =
			this.middlewareDatasetService.getDatasets(studyId, Collections.set(DatasetTypeEnum.SUMMARY_DATA.getId())).get(0);
		return inputVariableDatasetMap.entrySet().stream()
			.filter(entry -> summaryDataset.getDatasetId().equals(entry.getValue()))
			.map(entry -> entry.getKey().toString())
			.collect(Collectors.toList());
	}

	protected void setProcessor(final DerivedVariableProcessor processor) {
		this.processor = processor;
	}

	protected void setResourceBundleMessageSource(final ResourceBundleMessageSource resourceBundleMessageSource) {
		this.resourceBundleMessageSource = resourceBundleMessageSource;
	}

}
