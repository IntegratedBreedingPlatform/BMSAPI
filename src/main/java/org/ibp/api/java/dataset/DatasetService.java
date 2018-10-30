package org.ibp.api.java.dataset;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetTrait;

public interface DatasetService {
	
	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);
	
	MeasurementVariable addDatasetTrait(Integer studyId, Integer datasetId, DatasetTrait datasetTrait);
	
}
