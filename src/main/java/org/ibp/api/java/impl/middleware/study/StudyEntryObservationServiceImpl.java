package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.dataset.StockPropertyData;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyEntryObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;

@Service
@Transactional
public class StudyEntryObservationServiceImpl implements StudyEntryObservationService {

	private BindingResult errors;

	private static final Integer VALUE_MAX_LENGTH = 255;

	@Autowired
	private org.generationcp.middleware.api.study.StudyEntryObservationService studyEntryObservationService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private StudyEntryValidator studyEntryValidator;

	@Override
	public Integer createObservation(final String programUUID, final Integer studyId,
		final Integer datasetId, final StockPropertyData stockPropertyData) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListObservationRequestDto.class.getName());
		BaseValidator.checkNotNull(stockPropertyData, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(stockPropertyData.getVariableId(), "param.null", new String[] {"variableId"});
		BaseValidator.checkNotNull(stockPropertyData.getStockId(), "param.null", new String[] {"stockPropertyId"});
		BaseValidator.checkNotNull(stockPropertyData.hasValue(), "param.null", new String[] {"value"});

		this.validateValue(stockPropertyData.getValue());
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Arrays.asList(stockPropertyData.getVariableId()));
		this.studyEntryValidator.validateStudyContainsEntries(studyId, Arrays.asList(stockPropertyData.getStockId()));
		this.studyEntryValidator.validateStudyEntryHasNotVariable(stockPropertyData.getStockId(), stockPropertyData.getVariableId());

		final Variable variable = this.ontologyVariableDataManager.getVariable(programUUID, stockPropertyData.getVariableId(), false);
		stockPropertyData
			.setCategoricalValueId(VariableValueUtil.resolveCategoricalValueId(variable, stockPropertyData.getValue()));

		return this.studyEntryObservationService.createObservation(studyId, stockPropertyData);
	}

	private void validateValue(final String value) {
		if (value.length() > VALUE_MAX_LENGTH) {
			this.errors.reject("study.entry.observation.invalid.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
