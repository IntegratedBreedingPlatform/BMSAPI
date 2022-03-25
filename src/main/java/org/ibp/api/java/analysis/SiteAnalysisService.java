package org.ibp.api.java.analysis;

import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.generationcp.middleware.service.impl.analysis.SummaryStatisticsImportRequest;
import org.ibp.api.rest.dataset.DatasetDTO;

public interface SiteAnalysisService {

	DatasetDTO createMeansDataset(String crop, Integer studyId, MeansImportRequest meansInput);

	DatasetDTO createSummaryStatisticsDataset(String crop, Integer studyId, SummaryStatisticsImportRequest summaryStatisticsImportRequest);
}
