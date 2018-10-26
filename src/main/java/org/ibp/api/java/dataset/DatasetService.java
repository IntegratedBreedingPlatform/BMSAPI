package org.ibp.api.java.dataset;

import java.util.List;

public interface DatasetService {
	
	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);
	
}
