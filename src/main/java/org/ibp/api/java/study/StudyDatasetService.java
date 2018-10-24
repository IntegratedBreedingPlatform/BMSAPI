package org.ibp.api.java.study;

import java.util.List;

public interface StudyDatasetService {
	
	long countPhenotypesForDataset(Integer datasetId, List<Integer> traitIds);
	
	boolean datasetExists(Integer studyId, Integer datasetId);

}
