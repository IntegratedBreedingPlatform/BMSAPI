package org.ibp.api.java.impl.middleware.analysis;

import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.ibp.api.java.analysis.SiteAnalysisService;
import org.ibp.api.java.impl.middleware.common.validator.MeansImportRequestValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SiteAnalysisServiceImpl implements SiteAnalysisService {

	@Autowired
	private org.generationcp.middleware.service.api.analysis.SiteAnalysisService middlewareSiteAnalysisService;

	@Autowired
	private DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private StudyEntryValidator studyEntryValidator;

	@Autowired
	private MeansImportRequestValidator meansImportRequestValidator;

	@Override
	public DatasetDTO createMeansDataset(final Integer studyId, final MeansImportRequest meansImportRequest) {

		this.studyValidator.validate(studyId, true);
		this.studyValidator.validateStudyHasNoMeansDataset(studyId);
		this.meansImportRequestValidator.validateMeansDataIsNotEmpty(meansImportRequest);
		this.meansImportRequestValidator.validateEnvironmentNumberIsNotEmpty(meansImportRequest);
		this.meansImportRequestValidator.validateDataValuesIsNotEmpty(meansImportRequest);
		this.meansImportRequestValidator.validateEntryNumberIsNotEmptyAndDistinctPerEnvironment(meansImportRequest);
		final Set<Integer> environmentNumbers =
			meansImportRequest.getData().stream().map(MeansImportRequest.MeansData::getEnvironmentNumber).collect(Collectors.toSet());
		this.studyValidator.validateStudyInstanceNumbers(studyId, environmentNumbers);
		final Set<String> entryNumbers =
			meansImportRequest.getData().stream().map(o -> String.valueOf(o.getEntryNo())).collect(Collectors.toSet());
		this.studyEntryValidator.validateStudyContainsEntryNumbers(studyId, entryNumbers);
		this.meansImportRequestValidator.validateAnalysisVariableNames(meansImportRequest);

		final int meansDatasetId = this.middlewareSiteAnalysisService.createMeansDataset(studyId, meansImportRequest);
		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(meansDatasetId);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final DatasetDTO returnValue = mapper.map(datasetDTO, DatasetDTO.class);
		returnValue.setStudyId(studyId);
		return returnValue;
	}
}
