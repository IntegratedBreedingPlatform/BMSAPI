package org.ibp.api.java.dataset;

import java.io.File;
import java.util.Set;

public interface DatasetCSVExportService extends DatasetExportService {

	@Override
	File export(int studyId, int datasetId, Set<Integer> instanceIds, int collectionOrderId);
}
