
package org.ibp.api.java.impl.middleware.dataset.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DatasetValidator {

	private static final List<VariableType> OBSERVATION_DATASET_VALID_VAR_TYPES =
		Arrays.asList(VariableType.TRAIT, VariableType.SELECTION_METHOD, VariableType.GERMPLASM_DESCRIPTOR, VariableType.ENTRY_DETAIL);

	private static final List<VariableType> ENVIRONMENT_DATASET_VALID_VAR_TYPES =
		Arrays.asList(VariableType.ENVIRONMENT_CONDITION, VariableType.ENVIRONMENT_DETAIL);

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private DatasetService middlewareDatasetService;

	@Resource
	private DatasetTypeService datasetTypeService;

	private BindingResult errors;

	public DatasetValidator() {
		// Empty constructor expected by MockMvcRequestBuilder
	}

	public void validateDataset(final Integer studyId, final Integer datasetId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		if (datasetId == null) {
			this.errors.reject("dataset.required", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final boolean isValidDatasetId = this.middlewareDatasetService.isValidDatasetId(datasetId);
		if (!isValidDatasetId) {
			this.errors.reject("dataset.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		this.validateDatasetBelongsToStudy(studyId, datasetId);
	}

	public void validateDataset(final Integer datasetId) {
		final DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(datasetId);
		if (datasetDTO == null) {
			this.errors.reject("dataset.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

	public void validateObservationDatasetType(final Integer datasetId) {
		final DatasetDTO datasetBasicDTO = this.middlewareDatasetService.getDataset(datasetId);
		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(datasetBasicDTO.getDatasetTypeId());
		if (!datasetType.isObservationType() && !datasetType.isSubObservationType()) {
			this.errors.reject("dataset.type.not.observation", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}
	}

	public StandardVariable validateDatasetVariable(
		final Integer studyId, final Integer datasetId, final DatasetVariable datasetVariable, final Boolean shouldBeDatasetVariable) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.validateDataset(studyId, datasetId);

		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);

		// Validate if variable exists and of supported variable type for given dataset type
		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(dataSet.getDatasetTypeId());
		final boolean studyHasExperimentDesign = dataSet.getInstances().stream().anyMatch(i -> i.isHasExperimentalDesign() == Boolean.TRUE);
		final VariableType variableType =
			this.validateDatasetVariableType(datasetType, datasetVariable.getVariableTypeId(), studyHasExperimentDesign);
		final Integer variableId = datasetVariable.getVariableId();
		final StandardVariable standardVariable =
			this.ontologyDataManager.getStandardVariable(variableId, ContextHolder.getCurrentProgram());

		final Map<Integer, MeasurementVariable> measurementVariableMap =
			dataSet.getVariables().stream().collect(Collectors.toMap(MeasurementVariable::getTermId, Function.identity()));

		this.validateVariable(standardVariable, variableType);
		this.validateIfDatasetVariableAlreadyExists(variableId, shouldBeDatasetVariable, measurementVariableMap, datasetType);
		this.validateNotAddingSystemEntryDetailsToAlreadyGeneratedExperiment(standardVariable, studyHasExperimentDesign);

		return standardVariable;
	}

	public void validateExistingDatasetVariables(
		final Integer studyId, final Integer datasetId, final List<Integer> variableIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.validateDataset(studyId, datasetId);

		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);
		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(dataSet.getDatasetTypeId());
		final boolean studyHasExperimentDesign = dataSet.getInstances().stream().anyMatch(i -> i.isHasExperimentalDesign() == Boolean.TRUE);

		final Map<Integer, MeasurementVariable> measurementVariableMap =
			dataSet.getVariables().stream().collect(Collectors.toMap(MeasurementVariable::getTermId, Function.identity()));

		for (final Integer variableId : variableIds) {
			// If the variable does not exist, MiddlewareQueryException will be thrown
			this.ontologyDataManager.getStandardVariable(variableId, ContextHolder.getCurrentProgram());
			this.validateIfDatasetVariableAlreadyExists(variableId, true, measurementVariableMap, datasetType);
			this.validateDatasetVariableType(datasetType, measurementVariableMap.get(variableId).getVariableType().getId(),
				studyHasExperimentDesign);
		}

	}

	void validateIfDatasetVariableAlreadyExists(
		final Integer variableId, final Boolean shouldAlreadyBeDatasetVariable,
		final Map<Integer, MeasurementVariable> measurementVariableMap, final DatasetTypeDTO datasetType) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (measurementVariableMap.isEmpty() && shouldAlreadyBeDatasetVariable) {
			this.errors.reject("variable.not.dataset.variable", new Integer[] {variableId}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		boolean isDatasetVariable = false;

		if (measurementVariableMap.containsKey(variableId)) {
			isDatasetVariable = true;
			final MeasurementVariable measurementVariable = measurementVariableMap.get(variableId);
			final VariableType variableType = measurementVariable.getVariableType();
			// Add Variable Scenario
			if (!shouldAlreadyBeDatasetVariable) {
				this.errors.reject("variable.already.dataset.variable", new Object[] {String.valueOf(variableId)}, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());

				// If variable was found, check it is a supported variable type for dataset
			} else if (this.isInvalidVariableTypeForDatasetType(datasetType, variableType)) {
				this.errors
					.reject("dataset.variable.cannot.be.deleted", new Object[] {String.valueOf(variableId), variableType.getName()},
						"");
				throw new NotSupportedException(this.errors.getAllErrors().get(0));
			}
		}

		// Either the variable is not a dataset variable or is of unsupported type. Only Trait, Selection Method and Observation Unit
		// variables in Middleware for observation dataset. For environment dataset, Environment Detail and Study Conditions are retrieved
		if (!isDatasetVariable && shouldAlreadyBeDatasetVariable) {
			this.errors.reject("variable.not.dataset.variable", new Object[] {String.valueOf(variableId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validatePlotDatasetType(final Integer datasetId) {
		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);
		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(dataSet.getDatasetTypeId());
		if (!datasetType.isObservationType()) {
			this.errors.reject("dataset.type.not.plot", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}
	}

	public VariableType validateVariableType(final Integer variableTypeId) {
		final VariableType variableType = VariableType.getById(variableTypeId);
		if (variableType == null) {
			this.errors.reject("variable.type.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
		return variableType;
	}

	private VariableType validateDatasetVariableType(final DatasetTypeDTO datasetType, final Integer variableTypeId,
		final Boolean studyHasExperimentDesign) {
		final VariableType variableType = this.validateVariableType(variableTypeId);
		if (this.isInvalidVariableTypeForDatasetType(datasetType, variableType)) {
			this.errors.reject("variable.type.not.supported", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}

		// Do not allow germplasm descriptor to be added or removed if study has experiment design.
		if (VariableType.GERMPLASM_DESCRIPTOR.equals(variableType) && studyHasExperimentDesign) {
			this.errors
				.reject("dataset.germplasm.descriptor.cannot.be.added.or.removed.study.has.experiment.design", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		return variableType;

	}

	private boolean isInvalidVariableTypeForDatasetType(final DatasetTypeDTO datasetType, final VariableType variableType) {
		return ((datasetType.isObservationType() && !OBSERVATION_DATASET_VALID_VAR_TYPES.contains(variableType)) || (
			DatasetTypeEnum.SUMMARY_DATA.getId() == datasetType.getDatasetTypeId() && !ENVIRONMENT_DATASET_VALID_VAR_TYPES
				.contains(variableType)));
	}

	void validateVariable(final StandardVariable variable, final VariableType variableType) {
		// Check if variable is configured to be given variable type
		if (!variable.getVariableTypes().contains(variableType)) {
			this.errors.reject("variable.not.of.given.variable.type", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateDatasetBelongsToStudy(final Integer studyId, final Integer datasetId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final boolean datasetIdsBelongToStudy =
			this.middlewareDatasetService.allDatasetIdsBelongToStudy(studyId, Collections.singletonList(datasetId));
		if (!datasetIdsBelongToStudy) {
			this.errors.reject("dataset.do.not.belong.to.study", new String[] {String.valueOf(datasetId), String.valueOf(studyId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateVariableBelongsToVariableType(final int datasetId, final int variableId, final int variableTypeId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		// Check if the variableId is included in the specified VariableType
		final boolean variableExists =
			this.middlewareDatasetService.getObservationSetVariables(datasetId, Arrays.asList(variableTypeId)).stream()
				.anyMatch(v -> v.getTermId() == variableId);
		if (!variableExists) {
			this.errors.reject("variable.does.not.belong.to.specified.variable.type", new String[] {String.valueOf(variableId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateNotRemovingSystemEntryDetailToAlreadyGeneratedExperiment(final Integer datasetId,
		final List<Integer> variableIds) {

		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);
		final boolean studyHasExperimentDesign = dataSet.getInstances().stream().anyMatch(StudyInstance::isHasExperimentalDesign);

		if (studyHasExperimentDesign) {
			final List<StandardVariable> variables =
				this.ontologyDataManager.getStandardVariables(variableIds, ContextHolder.getCurrentProgram());
			final String systemVariableNames = variables.stream()
				.filter(variable -> variable.getVariableTypes().contains(VariableType.ENTRY_DETAIL) && variable.isSystem())
				.map(StandardVariable::getName)
				.collect(Collectors.joining(", "));
			if (!StringUtils.isEmpty(systemVariableNames)) {
				this.errors.reject("system-variable.cannot.remove", new String[] {String.valueOf(systemVariableNames)},  "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateNotAddingSystemEntryDetailsToAlreadyGeneratedExperiment(final StandardVariable variable,
		final boolean studyHasExperimentDesign) {
		if (studyHasExperimentDesign && variable.getVariableTypes().contains(VariableType.ENTRY_DETAIL) && variable.isSystem()) {
			this.errors.reject("system-variable.cannot.add", new String[] {String.valueOf(variable.getName())},  "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
