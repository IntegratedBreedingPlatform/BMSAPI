
package org.ibp.api.java.impl.middleware.dataset.validator;

import java.util.HashMap;

import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Component
public class DatasetValidator {

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private BindingResult errors;

	public void validateDataset(final Integer studyId, final Integer datasetId, final Boolean shouldBeSubobservationDataset) {

		final DataSet dataSet = this.studyDataManager.getDataSet(datasetId);
		this.validateDataset(dataSet, shouldBeSubobservationDataset);
	}

	private void validateDataset(final DataSet dataSet, final Boolean shouldBeSubobservationDataset) {
		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		
		if (dataSet == null) {
			this.errors.reject("dataset.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		// TODO add validation here that dataset is valid dataset of study

		if (shouldBeSubobservationDataset && !DataSetType.isSubObservationDatasetType(dataSet.getDataSetType())) {
			this.errors.reject("dataset.type.not.subobservation", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}
	}

	public StandardVariable validateDatasetTrait(final Integer studyId, final Integer datasetId,
			final Boolean shouldBeSubobservationDataset, final Integer traitId, final Boolean shouldAlreadyBeDatasetTrait) {
		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		
		final DataSet dataSet = this.studyDataManager.getDataSet(datasetId);
		this.validateDataset(dataSet, shouldBeSubobservationDataset);

		// Validate if variable exists and is TRAIT variable type
		final StandardVariable traitVariable = this.ontologyDataManager.getStandardVariable(traitId, dataSet.getProgramUUID());
		this.validateTraitVariable(traitVariable);

		this.validateIfTraitAlreadyDatasetVariable(traitId, shouldAlreadyBeDatasetTrait, dataSet);

		return traitVariable;
	}

	private void validateIfTraitAlreadyDatasetVariable(final Integer traitId, final Boolean shouldAlreadyBeDatasetTrait,
			final DataSet dataSet) {
		final VariableTypeList variableList = dataSet.getVariableTypes();
		final VariableTypeList variates = variableList.getVariates();
		if (variates == null && shouldAlreadyBeDatasetTrait) {
			this.errors.reject("trait.not.dataset.variable", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		boolean isDatasetTrait = false;
		for (final DMSVariableType datasetVariable : variates.getVariableTypes()) {
			if (VariableType.TRAIT.equals(datasetVariable.getVariableType()) && traitId.equals(datasetVariable.getId())) {
				isDatasetTrait = true;
			}
		}

		if (isDatasetTrait && !shouldAlreadyBeDatasetTrait) {
			this.errors.reject("trait.already.dataset.variable", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!isDatasetTrait && shouldAlreadyBeDatasetTrait) {
			this.errors.reject("trait.not.dataset.variable", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	void validateTraitVariable(final StandardVariable traitVariable) {
		if (traitVariable == null) {
			this.errors.reject("trait.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		if (!traitVariable.getVariableTypes().contains(VariableType.TRAIT)) {
			this.errors.reject("variable.not.trait", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}
	}

}
