package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationRequestDto;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.StockProperty;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.StockPropertyData;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyEntryObservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class StudyEntryObservationServiceImpl implements StudyEntryObservationService {

	private BindingResult errors;

	private static final Integer VALUE_MAX_LENGTH = 255;

	@Resource
	private org.generationcp.middleware.api.study.StudyEntryObservationService studyEntryObservationService;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private StudyEntryService middlewareStudyEntryService;

	@Resource
	protected StudyDataManager studyDataManager;

	@Resource
	protected DatasetService datasetService;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private DatasetValidator datasetValidator;

	@Resource
	private StudyEntryValidator studyEntryValidator;

	@Override
	public Integer createObservation(final Integer studyId, final StockPropertyData stockPropertyData) {
		this.commonValidations(studyId, stockPropertyData, true);
		this.validateStudyEntryVariableShouldNotExist(stockPropertyData.getStockId(), stockPropertyData.getVariableId());

		this.setCategoricalValueId(stockPropertyData);
		return this.studyEntryObservationService.createObservation(stockPropertyData);
	}

	@Override
	public Integer updateObservation(final Integer studyId, final StockPropertyData stockPropertyData) {
		this.commonValidations(studyId, stockPropertyData, false);
		this.validateStudyEntryVariableShouldExist(stockPropertyData.getStockId(), stockPropertyData.getVariableId());
		this.validateNotUpdateEntryTypeIfDesignWasGenerated(studyId, stockPropertyData.getVariableId());

		this.setCategoricalValueId(stockPropertyData);
		return this.studyEntryObservationService.updateObservation(stockPropertyData);
	}

	@Override
	public void deleteObservation(final Integer studyId, final Integer stockPropertyId) {
		BaseValidator.checkNotNull(stockPropertyId, "param.null", new String[] {"stockPropertyId"});

		this.studyValidator.validate(studyId, true);
		this.validateObservationBelongsToStudy(studyId, stockPropertyId);

		this.studyEntryObservationService.deleteObservation(stockPropertyId);
	}

	@Override
	public long countObservationsByVariables(final Integer studyId, final List<Integer> variableIds) {
		this.studyValidator.validate(studyId, false);

		final DataSet dataSet = this.studyValidator.validateStudyHasPlotDataset(studyId);
		this.datasetValidator.validateExistingDatasetVariables(studyId, dataSet.getId(), variableIds);

		return this.studyEntryObservationService.countObservationsByStudyAndVariables(studyId, variableIds);
	}

	private void validateValue(final String value) {
		if (value.length() > VALUE_MAX_LENGTH) {
			this.errors.reject("study.entry.observation.invalid.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void commonValidations(final Integer studyId, final StockPropertyData stockPropertyData, final boolean validateCreation) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListObservationRequestDto.class.getName());
		BaseValidator.checkNotNull(stockPropertyData, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(stockPropertyData.getVariableId(), "param.null", new String[] {"variableId"});
		BaseValidator.checkNotNull(stockPropertyData.getStockId(), "param.null", new String[] {"stockPropertyId"});
		BaseValidator.checkNotNull(stockPropertyData.hasValue(), "param.null", new String[] {"value"});

		this.validateValue(stockPropertyData.getValue());
		final Variable variable = this.validateVariableIdExists(stockPropertyData.getVariableId());
		if (validateCreation && TermId.ENTRY_NO.getId() == variable.getId()) {
			this.errors.reject("study.entry.observation.cannot.add.entry.number", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateVariableDataTypeValue(variable, stockPropertyData.getValue());

		this.studyValidator.validate(studyId, true);

		final DataSet dataSet = this.studyValidator.validateStudyHasPlotDataset(studyId);
		this.datasetValidator.validateExistingDatasetVariables(studyId, dataSet.getId(), Arrays.asList(stockPropertyData.getVariableId()));
		this.studyEntryValidator.validateStudyContainsEntries(studyId, Arrays.asList(stockPropertyData.getStockId()));
	}

	private void validateStudyEntryVariableShouldExist(final Integer entryId, final Integer variableId) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.middlewareStudyEntryService.getByStockIdAndTypeId(entryId, variableId).orElseThrow(() -> {
			this.errors.reject("study.entry.variable.not-found", new String[] {String.valueOf(entryId), String.valueOf(variableId)}, "");
			return new ApiRequestValidationException(this.errors.getAllErrors());
		});
	}

	private void validateStudyEntryVariableShouldNotExist(final Integer entryId, final Integer variableId) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.middlewareStudyEntryService.getByStockIdAndTypeId(entryId, variableId).ifPresent(stockProperty -> {
			this.errors.reject("study.entry.variable.already-has", new String[] {String.valueOf(entryId), String.valueOf(variableId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		});
	}

	private Variable validateVariableIdExists(final Integer variableId) {
		final Variable variable =
			this.ontologyVariableDataManager.getVariable(null, variableId, false);
		if (variable == null) {
			this.errors.reject("study.entry.invalid.variable", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		return variable;
	}

	private void validateVariableDataTypeValue(final Variable variable, final String value) {
		if (!VariableValueUtil.isValidAttributeValue(variable, value)) {
			this.errors.reject("invalid.variable.value", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateObservationBelongsToStudy(final Integer studyId, final Integer observationId) {
		final StockProperty stockProperty = this.middlewareStudyEntryService.getByStockPropertyId(observationId);
		if (stockProperty == null) {
			this.errors.reject("study.entry.observation.not-found", new String[] {String.valueOf(observationId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!stockProperty.getStock().getProject().getProjectId().equals(studyId)) {
			this.errors.reject("study.entry.observation.must.belong.to.study", new String[] {String.valueOf(observationId), String.valueOf(studyId)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNotUpdateEntryTypeIfDesignWasGenerated(final Integer studyId, final Integer variableId) {
		if (TermId.ENTRY_TYPE.getId() == variableId) {
			final DataSet dataSet = this.studyValidator.validateStudyHasPlotDataset(studyId);
			if (this.studyDataManager.countExperiments(dataSet.getId()) > 0) {
				this.errors.reject("study.entry.observation.cannot.update.entry-type", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

	private void setCategoricalValueId(final StockPropertyData stockPropertyData) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(null, stockPropertyData.getVariableId(), false);
		stockPropertyData
			.setCategoricalValueId(VariableValueUtil.resolveCategoricalValueId(variable, stockPropertyData.getValue()));
	}

}
