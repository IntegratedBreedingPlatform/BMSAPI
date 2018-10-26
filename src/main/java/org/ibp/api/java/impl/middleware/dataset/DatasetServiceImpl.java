package org.ibp.api.java.impl.middleware.dataset;

import org.springframework.stereotype.Service;

import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import java.util.List;
import org.ibp.api.java.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {
	
	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Override
	public Integer generateSubObservationDataset(final Integer studyId, final DatasetGeneratorInput datasetGeneratorInput) {

		this.studyValidator.validate(studyId, true);

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
