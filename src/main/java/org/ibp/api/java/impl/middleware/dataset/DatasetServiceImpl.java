package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Override
	public Integer generateSubObservationDataset(final DatasetGeneratorInput datasetGeneratorInput) {
		return datasetService.generateSubObservationDataset(null, null, null, null, null, null);
	}

}
