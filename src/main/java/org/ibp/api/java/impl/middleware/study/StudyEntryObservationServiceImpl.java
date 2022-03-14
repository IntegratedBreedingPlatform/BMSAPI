package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
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
	private StudyValidator studyValidator;

	@Resource
	private DatasetValidator datasetValidator;

	@Resource
	private StudyEntryValidator studyEntryValidator;

	@Override
	public Integer createObservation(final Integer studyId,	final Integer datasetId, final StockPropertyData stockPropertyData) {
		this.commonValidations(studyId, datasetId, stockPropertyData);
		this.validateStudyEntryVariableShouldNotExist(stockPropertyData.getStockId(), stockPropertyData.getVariableId());

		this.setStockPropertyValue(stockPropertyData);
		return this.studyEntryObservationService.createObservation(stockPropertyData);
	}

	@Override
	public Integer updateObservation(final Integer studyId, final Integer datasetId, final StockPropertyData stockPropertyData) {
		this.commonValidations(studyId, datasetId, stockPropertyData);
		this.validateStudyEntryVariableShouldExist(stockPropertyData.getStockId(), stockPropertyData.getVariableId());

		this.setStockPropertyValue(stockPropertyData);
		return this.studyEntryObservationService.updateObservation(stockPropertyData);
	}

	private void validateValue(final String value) {
		if (value.length() > VALUE_MAX_LENGTH) {
			this.errors.reject("study.entry.observation.invalid.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void commonValidations(final Integer studyId,	final Integer datasetId, final StockPropertyData stockPropertyData) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListObservationRequestDto.class.getName());
		BaseValidator.checkNotNull(stockPropertyData, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(stockPropertyData.getVariableId(), "param.null", new String[] {"variableId"});
		BaseValidator.checkNotNull(stockPropertyData.getStockId(), "param.null", new String[] {"stockPropertyId"});
		BaseValidator.checkNotNull(stockPropertyData.hasValue(), "param.null", new String[] {"value"});

		this.validateValue(stockPropertyData.getValue());
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Arrays.asList(stockPropertyData.getVariableId()));
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

	private void setStockPropertyValue(final StockPropertyData stockPropertyData) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(null, stockPropertyData.getVariableId(), false);
		stockPropertyData
			.setCategoricalValueId(VariableValueUtil.resolveCategoricalValueId(variable, stockPropertyData.getValue()));
	}

}
