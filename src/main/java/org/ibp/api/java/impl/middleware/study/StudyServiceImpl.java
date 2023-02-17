
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.api.study.StudyDetailsDTO;
import org.generationcp.middleware.api.study.StudySearchRequest;
import org.generationcp.middleware.api.study.StudySearchResponse;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.name.validator.GermplasmNameTypeValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {

	@Autowired
	private DatasetService middlewareDatasetService;

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Autowired
	private org.generationcp.middleware.service.api.SampleService sampleService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private GermplasmNameTypeValidator germplasmNameTypeValidator;

	@Override
	public String getProgramUUID(final Integer studyIdentifier) {
		return this.middlewareStudyService.getProgramUUID(studyIdentifier);
	}


	@Override
	public Boolean isSampled(final Integer studyId) {
		try {
			this.studyValidator.validate(studyId, false);
			return this.sampleService.studyHasSamples(studyId);
		} catch (final MiddlewareException e) {
			throw new ApiRuntime2Exception("", "an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public List<StudyTypeDto> getStudyTypes() {
		try {
			return this.studyDataManager.getAllVisibleStudyTypes();
		} catch (final MiddlewareException e) {
			throw new ApiRuntime2Exception("", "an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public StudyReference getStudyReference(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.studyDataManager.getStudyReference(studyId);
	}

	@Override
	public void updateStudy(final Study study) {
		final int studyId = study.getId();
		this.studyValidator.validate(studyId, false);
		this.studyDataManager.updateStudyLockedStatus(studyId, study.isLocked());
	}

	@Override
	public Integer getEnvironmentDatasetId(final Integer studyId) {
		final List<DatasetDTO> datasets =
			this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!CollectionUtils.isEmpty(datasets)) {
			return datasets.get(0).getDatasetId();
		} else {
			throw new ApiRuntime2Exception("","No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}
	}

	@Override
	public List<GermplasmStudyDto> getGermplasmStudies(final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		return this.middlewareStudyService.getGermplasmStudies(gid);
	}

	@Override
	public void deleteStudy(final Integer studyId) {
		this.studyValidator.validateDeleteStudy(studyId);
		this.middlewareStudyService.deleteStudy(studyId);
	}

	@Override
	public void deleteNameTypeFromStudies(final Integer nameTypeId) {
		this.germplasmNameTypeValidator.validate(nameTypeId);
		this.middlewareStudyService.deleteNameTypeFromStudies(nameTypeId);
	}

	@Override
	public List<StudySearchResponse> searchStudies(final String programUUID, final StudySearchRequest studySearchRequest,
		final Pageable pageable) {
		return this.middlewareStudyService.searchStudies(programUUID, studySearchRequest, pageable);
	}

	@Override
	public long countSearchStudies(final String programUUID, final StudySearchRequest studySearchRequest) {
		return this.middlewareStudyService.countSearchStudies(programUUID, studySearchRequest);
	}

	@Override
	public StudyDetailsDTO getStudyDetails(final String programUUID, final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.middlewareStudyService.getStudyDetails(programUUID, studyId);
	}

}
