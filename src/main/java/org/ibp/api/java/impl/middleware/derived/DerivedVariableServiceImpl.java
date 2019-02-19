package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.derived.DerivedVariableService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class DerivedVariableServiceImpl implements DerivedVariableService {

	private static final Logger LOG = LoggerFactory.getLogger(DerivedVariableServiceImpl.class);
	public static final String HAS_DATA_OVERWRITE_RESULT_KEY = "hasDataOverwrite";
	public static final String INPUT_MISSING_DATA_RESULT_KEY = "inputMissingData";

	@Resource
	private DatasetService datasetService;

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

	public DerivedVariableServiceImpl() {
		// do nothing
	}

	@Override
	public Map<String, Object> execute(
		final int studyId, final int datasetId, final Integer variableId, final List<Integer> geoLocationIds,
		final boolean overwriteExistingData) {

		final Map<String, Object> results = new HashMap<>();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, true);
		this.derivedVariableValidator.validate(variableId, geoLocationIds);
		this.derivedVariableValidator.verifyMissingInputVariables(variableId, datasetId);

		// Get the list of observation unit rows grouped by intances
		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap =
			datasetService.getInstanceIdToObservationUnitRowsMap(studyId, datasetId, geoLocationIds);
		// Get the the measurement variables of all traits in a dataset so that we can determine the datatype and possibleValues
		// of a ObservationUnitData.
		final Map<Integer, MeasurementVariable> measurementVariablesMap = this.createVariableIdMeasurementVariableMap(datasetId);

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(variableId);
		final FormulaDto formula = formulaOptional.get();
		final Map<String, Object> parameters = DerivedVariableUtils.extractParameters(formula.getDefinition());

		// Calculate
		final Set<String> inputMissingData = new HashSet<>();

		// Iterate through the observations for each instances
		for (final List<ObservationUnitRow> observations : instanceIdObservationUnitRowsMap.values()) {
			for (final ObservationUnitRow observation : observations) {

				// Get input data
				final Set<String> rowInputMissingData = new HashSet<>();
				try {
					DerivedVariableUtils.extractValues(parameters, observation, measurementVariablesMap, rowInputMissingData);
				} catch (ParseException e) {
					LOG.error("Error parsing date value for parameters " + parameters, e);
					errors.reject("study.execute.calculation.parsing.exception");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
				inputMissingData.addAll(rowInputMissingData);

				if (!rowInputMissingData.isEmpty() || parameters.values().contains("")) {
					continue;
				}

				// Evaluate
				String value;
				try {
					final String executableFormula = DerivedVariableUtils.replaceDelimiters(formula.getDefinition());
					value = this.processor.evaluateFormula(executableFormula, parameters);
				} catch (final Exception e) {
					LOG.error("Error evaluating formula " + formula + " with inputs " + parameters, e);
					errors.reject("study.execute.calculation.engine.exception");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}

				if (StringUtils.isBlank(value)) {
					continue;
				}

				// Process calculation result
				final ObservationUnitData target = observation.getVariables().get(formula.getTarget().getName());
				final MeasurementVariable targetMeasurementVariable = measurementVariablesMap.get(formula.getTarget().getId());

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

				this.saveCalculatedResult(
					value, categoricalId, observation.getObservationUnitId(),
					target.getObservationId(),
					targetMeasurementVariable);

				if (StringUtils.isNotEmpty(target.getValue()) && !target.getValue().equals(value)) {
					if (!overwriteExistingData) {
						// If there is an existing measurement data and the user did not explicitly choose to overwrite it, then throw a runtime exception
						// to rollback transaction so to prevent saving of calculated value.
						errors.reject("study.execute.calculation.has.existing.data");
						throw new OverwriteDataException(errors.getAllErrors());
					} else {
						// Else, just warn the user that there's data to overwrite.
						results.put(HAS_DATA_OVERWRITE_RESULT_KEY, true);
					}

				}

			}

		}

		// Process response
		if (!inputMissingData.isEmpty()) {
			// warn the user that there's missing data from input variables.
			results.put(
				INPUT_MISSING_DATA_RESULT_KEY, this.resourceBundleMessageSource
					.getMessage("study.execute.calculation.missing.data", new String[] {StringUtils.join(inputMissingData.toArray())},
						Locale.getDefault()));
		}

		return results;

	}

	/**
	 * Gets the list of formula dependencies of all derived variables that are not yet loaded in a dataset.
	 *
	 * @param studyId
	 * @param datasetId
	 * @return
	 */
	@Override
	public Set<String> getDependencyVariables(final int studyId, final int datasetId) {

		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		final Set<Integer> variableIdsOfTraitsInStudy = this.getVariableIdsOfTraitsInDataset(datasetId);
		final Set<String> derivedVariablesDependencies = new HashSet<>();

		final Set<FormulaVariable> formulaVariables = this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy);
		for (final FormulaVariable formulaVariable : formulaVariables) {
			if (!variableIdsOfTraitsInStudy.contains(formulaVariable.getId())) {
				derivedVariablesDependencies.add(formulaVariable.getName());
			}
		}

		return derivedVariablesDependencies;
	}

	/**
	 * Gets the list of formula dependencies of specific derived variables that are not yet loaded in a dataset.
	 *
	 * @param studyId
	 * @param datasetId
	 * @param variableId
	 * @return
	 */
	@Override
	public Set<String> getDependencyVariables(final int studyId, final int datasetId, final int variableId) {

		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		final List<Integer> variableIds = Arrays.asList(variableId);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, false, variableIds);

		final Set<Integer> variableIdsOfTraitsInStudy = this.getVariableIdsOfTraitsInDataset(datasetId);
		final Set<String> derivedVariablesDependencies = new HashSet<>();
		final Set<FormulaVariable> formulaVariables = this.formulaService.getAllFormulaVariables(new HashSet<Integer>(variableIds));
		for (final FormulaVariable formulaVariable : formulaVariables) {
			if (!variableIdsOfTraitsInStudy.contains(formulaVariable.getId())) {
				derivedVariablesDependencies.add(formulaVariable.getName());
			}
		}

		return derivedVariablesDependencies;
	}

	@Override
	public long countCalculatedVariablesInDatasets(final int studyId, final Set<Integer> datasetIds) {
		this.studyValidator.validate(studyId, false);
		for (final int datasetId : datasetIds) {
			this.datasetValidator.validateDataset(studyId, datasetId, false);
		}
		return this.datasetService.countCalculatedVariablesInDatasets(datasetIds);
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

	protected void saveCalculatedResult(
		final String value, final Integer categoricalId, final Integer observationUnitId, final Integer observationId,
		final MeasurementVariable measurementVariable) {

		// Update phenotype if it already exists, otherwise, create new phenotype.
		if (observationId != null) {
			this.datasetService.updatePhenotype(observationId, categoricalId, value);
		} else {
			final ObservationDto observationDto = new ObservationDto();
			observationDto.setVariableId(measurementVariable.getTermId());
			observationDto.setCategoricalValueId(categoricalId);
			observationDto.setObservationUnitId(observationUnitId);
			observationDto.setValue(value);
			this.datasetService.createPhenotype(observationDto);
		}

	}

	protected Map<Integer, MeasurementVariable> createVariableIdMeasurementVariableMap(final int datasetId) {
		final Map<Integer, MeasurementVariable> variableIdMeasurementVariableMap = new HashMap<>();
		final List<MeasurementVariable> measurementVariables =
			this.datasetService.getMeasurementVariables(datasetId, Arrays.asList(VariableType.TRAIT.getId()));
		for (final MeasurementVariable measurementVariable : measurementVariables) {
			variableIdMeasurementVariableMap.put(measurementVariable.getTermId(), measurementVariable);
		}
		return variableIdMeasurementVariableMap;
	}

}
