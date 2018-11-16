package org.ibp.api.java.dataset;

import java.io.File;
import java.util.Set;

public interface DatasetExportService {

	File exportAsCSV(final int studyId, final int datasetId, final Set<Integer> instanceIds);

}
