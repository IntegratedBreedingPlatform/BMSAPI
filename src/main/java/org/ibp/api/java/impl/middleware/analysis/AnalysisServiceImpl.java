package org.ibp.api.java.impl.middleware.analysis;

import org.generationcp.middleware.api.analysis.MeansRequestDto;
import org.ibp.api.java.analysis.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnalysisServiceImpl implements AnalysisService {

	@Autowired
	private org.generationcp.middleware.api.analysis.AnalysisService analysisMiddlewareService;

	@Override
	public void importMeans(final MeansRequestDto meansInput) {
		this.analysisMiddlewareService.importMeansData(meansInput);
	}
}
