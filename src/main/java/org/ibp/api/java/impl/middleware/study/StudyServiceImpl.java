
package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v2.trial.TrialImportResponse;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.study.validator.TrialImportRequestValidator;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DatasetService middlewareDatasetService;

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Autowired
	org.generationcp.middleware.service.api.SampleService sampleService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private TrialImportRequestValidator trialImportRequestDtoValidator;

	public TrialObservationTable getTrialObservationTable(final int studyIdentifier) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier);
	}

	@Override
	public String getProgramUUID(final Integer studyIdentifier) {
		return this.middlewareStudyService.getProgramUUID(studyIdentifier);
	}

	@Override
	public TrialObservationTable getTrialObservationTable(final int studyIdentifier, final Integer studyDbId) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier, studyDbId);
	}

	@Override
	public StudyDetailsDto getStudyDetailsByGeolocation(final Integer geolocationId) {
		return this.middlewareStudyService.getStudyDetailsByInstance(geolocationId);
	}

	@Override
	public List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(final StudySearchFilter studySearchFilter,
		final Pageable pageable) {
		return this.middlewareStudyService.getStudies(studySearchFilter, pageable);
	}

	@Override
	public long countStudies(final StudySearchFilter studySearchFilter) {
		return this.middlewareStudyService.countStudies(studySearchFilter);
	}

	@Override
	public List<PhenotypeSearchDTO> searchPhenotypes(final Integer pageSize, final Integer pageNumber,
		final PhenotypeSearchRequestDTO requestDTO) {
		return this.middlewareStudyService.searchPhenotypes(pageSize, pageNumber, requestDTO);
	}

	@Override
	public long countPhenotypes(final PhenotypeSearchRequestDTO requestDTO) {
		return this.middlewareStudyService.countPhenotypes(requestDTO);
	}

	@Override
	public Boolean isSampled(final Integer studyId) {
		try {
			this.studyValidator.validate(studyId, false);
			return this.sampleService.studyHasSamples(studyId);
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public List<StudyTypeDto> getStudyTypes() {
		try {
			return this.studyDataManager.getAllVisibleStudyTypes();
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
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
	public long countStudyInstances(final StudySearchFilter studySearchFilter) {
		return this.middlewareStudyService.countStudyInstances(studySearchFilter);
	}

	@Override
	public List<StudyInstanceDto> getStudyInstances(final StudySearchFilter studySearchFilter, final Pageable pageable) {
		return this.middlewareStudyService.getStudyInstances(studySearchFilter, pageable);
	}

	@Override
	public List<StudyInstanceDto> getStudyInstancesWithMetadata(final StudySearchFilter studySearchFilter, final Pageable pageable) {
		return this.middlewareStudyService.getStudyInstancesWithMetadata(studySearchFilter, pageable);
	}

	@Override
	public List<TreeNode> getStudyTree(final String parentKey, final String programUUID) {
		List<TreeNode> nodes = new ArrayList<>();
		if (StringUtils.isBlank(parentKey)) {
			final TreeNode rootNode = new TreeNode(AppConstants.STUDIES.name(), AppConstants.STUDIES.getString(), true, null);
			nodes.add(rootNode);
		} else if (parentKey.equals(AppConstants.STUDIES.name())) {
			final List<Reference> children = this.studyDataManager.getRootFolders(programUUID);
			nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(children, true);
		} else if (NumberUtils.isNumber(parentKey)) {
			final List<Reference> folders = this.studyDataManager.getChildrenOfFolder(Integer.valueOf(parentKey), programUUID);
			nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true);
		}
		return nodes;
	}

	@Override
	public Integer getEnvironmentDatasetId(final Integer studyId) {
		final List<DatasetDTO> datasets =
			this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!CollectionUtils.isEmpty(datasets)) {
			return datasets.get(0).getDatasetId();
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}
	}

	@Override
	public List<GermplasmStudyDto> getGermplasmStudies(final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		return this.middlewareStudyService.getGermplasmStudies(gid);
	}

	@Override
	public TrialImportResponse createTrials(String cropName, List<TrialImportRequestDTO> trialImportRequestDTOs) {
		final TrialImportResponse response = new TrialImportResponse();
		final int originalListSize = trialImportRequestDTOs.size();
		int noOfCreatedTrials = 0;

		// Remove germplasm that fails any validation. They will be excluded from creation
		final BindingResult bindingResult = this.trialImportRequestDtoValidator.pruneTrialsInvalidForImport(trialImportRequestDTOs, cropName);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}
		if (!CollectionUtils.isEmpty(trialImportRequestDTOs)) {

			final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
			final List<StudySummary> studySummaries = this.middlewareStudyService.saveStudies(cropName, trialImportRequestDTOs, user.getUserid());
			if (!CollectionUtils.isEmpty(studySummaries)) {
				noOfCreatedTrials = studySummaries.size();
			}
			response.setStudySummaries(studySummaries);
		}
		response.setStatus(noOfCreatedTrials + " out of " + originalListSize + " trials created successfully.");
		return response;
	}

	public void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}


	public void setStudyValidator(final StudyValidator studyValidator) {
		this.studyValidator = studyValidator;
	}

	public void setMiddlewareStudyService(final org.generationcp.middleware.service.api.study.StudyService middlewareStudyService) {
		this.middlewareStudyService = middlewareStudyService;
	}

	public void setGermplasmValidator(final GermplasmValidator germplasmValidator) {
		this.germplasmValidator = germplasmValidator;
	}

}
