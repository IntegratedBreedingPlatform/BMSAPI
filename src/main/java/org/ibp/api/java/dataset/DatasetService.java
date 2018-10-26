package org.ibp.api.java.dataset;

import java.util.List;

public interface DatasetService {
	
	long countPhenotypesForDataset(Integer datasetId, List<Integer> traitIds);
	
}
