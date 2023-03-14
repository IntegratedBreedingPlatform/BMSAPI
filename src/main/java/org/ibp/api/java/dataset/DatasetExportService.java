package org.ibp.api.java.dataset;

import java.io.File;
import java.util.Set;

public interface DatasetExportService {

	File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId,
		final boolean singleFile, final boolean includeSampleGenotpeValues);
}
