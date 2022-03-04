package org.ibp.api.java.impl.middleware.analysis;

import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.ibp.api.java.analysis.AnalysisService;
import org.ibp.api.java.impl.middleware.common.validator.MeansImportRequestValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnalysisServiceImpl implements AnalysisService {

	@Autowired
	private org.generationcp.middleware.service.api.analysis.AnalysisService middlewareAnalysisService;

	@Autowired
	private DatasetService middlewareDatasetService;

	@Autowired
	private MeansImportRequestValidator meansImportRequestValidator;

	@Override
	public DatasetDTO createMeansDataset(final MeansImportRequest meansImportRequest) {
		this.meansImportRequestValidator.validate(meansImportRequest);
		final int meansDatasetId = this.middlewareAnalysisService.createMeansDataset(meansImportRequest);
		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(meansDatasetId);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final DatasetDTO returnValue = mapper.map(datasetDTO, DatasetDTO.class);
		returnValue.setStudyId(meansImportRequest.getStudyId());
		return returnValue;
	}
}
