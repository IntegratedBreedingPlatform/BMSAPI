
package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Component
public class DatasetValidator {
	
	private static final Logger LOG = LoggerFactory.getLogger(DatasetValidator.class);
	
	private static final List<VariableType> VALID_VARIABLE_TYPES = 
			Arrays.asList(VariableType.TRAIT, VariableType.SELECTION_METHOD);

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private DatasetService middlewareDatasetService;
	
	@Autowired
	private ContextUtil contextUtil;

	private BindingResult errors;

	public DatasetValidator() {
		// Empty constructor expected by MockMvcRequestBuilder
	}

	public void validateDataset(final Integer studyId, final Integer datasetId, final Boolean shouldBeSubobservationDataset) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);
		this.validateDataset(studyId, dataSet, shouldBeSubobservationDataset);
	}

	private void validateDataset(final Integer studyId, final DatasetDTO dataSet, final Boolean shouldBeSubobservationDataset) {
		
		if (dataSet == null) {
			this.errors.reject("dataset.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		this.validateDatasetBelongsToStudy(studyId, dataSet.getDatasetId());

		if (shouldBeSubobservationDataset && !DataSetType.isSubObservationDatasetType(DataSetType.findById(dataSet.getDatasetTypeId()))) {
			this.errors.reject("dataset.type.not.subobservation", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}
	}

	public StandardVariable validateDatasetVariable(final Integer studyId, final Integer datasetId,
			final Boolean shouldBeSubobservationDataset, final DatasetVariable datasetVariable, final Boolean shouldAlreadyBeDatasetVariable) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);
		this.validateDataset(studyId, dataSet, shouldBeSubobservationDataset);

		// Validate if variable exists and of supported variable type
		final VariableType variableType = this.validateVariableType(datasetVariable.getVariableTypeId());
		final Integer variableId = datasetVariable.getVariableId();
		final StandardVariable standardVariable = this.ontologyDataManager.getStandardVariable(variableId, this.contextUtil.getCurrentProgramUUID());
		this.validateVariable(standardVariable, variableType, variableId);

		this.validateIfAlreadyDatasetVariable(variableId, shouldAlreadyBeDatasetVariable, dataSet);

		return standardVariable;
	}

	public void validateExistingDatasetVariables(final Integer studyId, final Integer datasetId,
			final Boolean shouldBeSubobservationDataset, final List<Integer> variableIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(datasetId);
		this.validateDataset(studyId, dataSet, shouldBeSubobservationDataset);

		for (final Integer variableId : variableIds) {
			// If the variable does not exist, MiddlewareQueryException will be thrown
			this.ontologyDataManager.getStandardVariable(variableId, this.contextUtil.getCurrentProgramUUID());
			this.validateIfAlreadyDatasetVariable(variableId, true, dataSet);
		}

	}

	public void validateIfAlreadyDatasetVariable(final Integer variableId, final Boolean shouldAlreadyBeDatasetVariable,
			final DatasetDTO dataSet) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final List<MeasurementVariable> datasetVariables = dataSet.getVariables();
		if (datasetVariables == null && shouldAlreadyBeDatasetVariable) {
			this.errors.reject("variable.not.dataset.variable", new Integer[] {variableId}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		boolean isDatasetVariable = false;
		for (final MeasurementVariable datasetVariable : datasetVariables) {
			if (variableId.equals(datasetVariable.getTermId())) {
				isDatasetVariable = true;
				final VariableType variableType = datasetVariable.getVariableType();
				if (!shouldAlreadyBeDatasetVariable) {
					this.errors.reject("variable.already.dataset.variable", new Object[] {String.valueOf(variableId)}, "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				} else if (!VALID_VARIABLE_TYPES.contains(variableType)) {
					this.errors.reject("dataset.variable.cannot.be.deleted", new Object[] {String.valueOf(variableId), variableType.getName()}, "");
					throw new NotSupportedException(this.errors.getAllErrors().get(0));
				}
			}
		}

		// Either the variable is not a dataset variable or is of unsupported type. Only Trait, Selection Method and Observation Unit
		// variables are retrieved in Middleware
		if (!isDatasetVariable && shouldAlreadyBeDatasetVariable) {
			this.errors.reject("variable.not.dataset.variable", new Object[] {String.valueOf(variableId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private VariableType validateVariableType(final Integer variableTypeId) {
		final VariableType variableType = VariableType.getById(variableTypeId);
		if (variableType == null) {
			this.errors.reject("variable.type.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		if (!VALID_VARIABLE_TYPES.contains(variableType)) {
			this.errors.reject("variable.type.not.supported", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}

		return variableType;

	}

	void validateVariable(final StandardVariable variable, final VariableType variableType, final Integer variableId) {
		// Check if variable is configured to be given variable type
		if (!variable.getVariableTypes().contains(variableType)) {
			this.errors.reject("variable.not.of.given.variable.type", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateDatasetBelongsToStudy(final Integer studyId, final Integer datasetId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final List<DatasetDTO> allChildren = this.middlewareDatasetService.getDatasets(studyId, new HashSet<Integer>());
		boolean found = false;
		for (final DatasetDTO datasetDTO: allChildren) {
			if (datasetDTO.getDatasetId().equals(datasetId)) {
				found = true;
				break;
			}
		}
		if (!found) {
			this.errors.reject("dataset.do.not.belong.to.study", new String[] {String.valueOf(datasetId), String.valueOf(studyId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}
}
