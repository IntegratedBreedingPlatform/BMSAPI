package org.ibp.api.java.analysis;

import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.ibp.api.rest.dataset.DatasetDTO;

public interface SiteAnalysisService {

	DatasetDTO createMeansDataset(final Integer studyId, MeansImportRequest meansInput);

}
