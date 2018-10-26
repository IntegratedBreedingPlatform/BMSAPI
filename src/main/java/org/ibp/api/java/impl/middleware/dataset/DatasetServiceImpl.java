package org.ibp.api.java.impl.middleware.dataset;

import java.util.List;

import org.ibp.api.java.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {
	
	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Override
	public long countPhenotypesForDataset(final Integer datasetId, final List<Integer> traitIds) {
		return this.middlewareDatasetService.countPhenotypes(datasetId, traitIds);
	}

}
