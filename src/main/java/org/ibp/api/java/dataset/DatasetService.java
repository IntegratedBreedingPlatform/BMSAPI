package org.ibp.api.java.dataset;

import org.ibp.api.rest.dataset.DatasetGeneratorInput;

import java.util.List;

public interface DatasetService {
	
	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);

	Integer generateSubObservationDataset(String cropName, Integer studyId, DatasetGeneratorInput datasetGeneratorInput);

}
