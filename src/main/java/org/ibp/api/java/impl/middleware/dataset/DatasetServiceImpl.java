package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetGeneratorInputValidator;
import org.springframework.stereotype.Service;

import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;

import java.util.HashMap;
import java.util.List;
import org.ibp.api.java.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {
	
	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetGeneratorInputValidator datasetGeneratorInputValidator;

	@Override
	public Integer generateSubObservationDataset(final String cropName, final Integer studyId, final DatasetGeneratorInput datasetGeneratorInput) {

		// checks that study exists and it is not locked
		this.studyValidator.validate(studyId, true);

		// checks input matches validation rules
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		this.datasetGeneratorInputValidator.validate(cropName, studyId, datasetGeneratorInput, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		// not implemented yet

		// conflict

		return middlewareDatasetService
				.generateSubObservationDataset(studyId, datasetGeneratorInput.getDatasetName(), datasetGeneratorInput.getDatasetTypeId(),
						Arrays.asList(datasetGeneratorInput.getInstanceIds()), datasetGeneratorInput.getSequenceVariableId(),
						datasetGeneratorInput.getNumberOfSubObservationUnits());
	}

	@Override
	public long countPhenotypes(final Integer studyId, final Integer datasetId, final List<Integer> traitIds) {
		this.studyValidator.validate(studyId, false);
		//FIXME - add validation if dataset is valid dataset of study (waiting on Middleware service to be available)
		return this.middlewareDatasetService.countPhenotypes(datasetId, traitIds);
	}

}
