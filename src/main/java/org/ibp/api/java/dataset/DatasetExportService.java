package org.ibp.api.java.dataset;

import java.io.File;
import java.util.Set;

public interface DatasetExportService {

	File exportAsCSV(int studyId, int datasetId, Set<Integer> instanceIds, int collectionOrderId, boolean isExportInSingleFile);
	
}
