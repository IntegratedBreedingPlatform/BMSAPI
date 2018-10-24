package org.ibp.api.java.impl.middleware.study;

import java.util.List;

import org.ibp.api.java.study.StudyDatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudyDatasetServiceImpl implements StudyDatasetService {
	
	@Autowired
	private org.generationcp.middleware.service.api.study.StudyDatasetService middlewareDatasetService;

	@Override
	public long countPhenotypesForDataset(final Integer datasetId, final List<Integer> traitIds) {
		return this.middlewareDatasetService.countPhenotypesForDataset(datasetId, traitIds);
	}

	@Override
	public boolean datasetExists(final Integer studyId, final Integer datasetId) {
		return this.middlewareDatasetService.datasetExists(studyId, datasetId);
	}

}
