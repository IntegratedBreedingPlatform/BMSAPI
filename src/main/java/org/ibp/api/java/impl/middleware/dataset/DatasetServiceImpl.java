package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Override
	public Integer generateSubObservationDataset(final Integer studyId, final DatasetGeneratorInput datasetGeneratorInput) {

		this.studyValidator.validate(studyId, true);

		return datasetService
				.generateSubObservationDataset(studyId, datasetGeneratorInput.getDatasetName(), datasetGeneratorInput.getDatasetTypeId(),
						Arrays.asList(datasetGeneratorInput.getInstanceIds()), datasetGeneratorInput.getSequenceVariableId(),
						datasetGeneratorInput.getNumberOfSubObservationUnits());
	}

}
