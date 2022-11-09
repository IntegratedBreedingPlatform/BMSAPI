
package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.study.StudyDTO;
import org.generationcp.middleware.api.study.StudySearchRequest;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Reference;
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

import java.util.ArrayList;
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
	org.generationcp.middleware.service.api.SampleService sampleService;

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
	public List<StudyDTO> getFilteredStudies(final String programUUID, final StudySearchRequest studySearchRequest,
		final Pageable pageable) {
		return this.middlewareStudyService.getFilteredStudies(programUUID, studySearchRequest, pageable);
	}

	@Override
	public long countFilteredStudies(final String programUUID, final StudySearchRequest studySearchRequest) {
		return this.middlewareStudyService.countFilteredStudies(programUUID, studySearchRequest);
	}

	@Override
	public void deleteNameTypeFromStudies(final Integer nameTypeId) {
		this.germplasmNameTypeValidator.validate(nameTypeId);
		this.middlewareStudyService.deleteNameTypeFromStudies(nameTypeId);
	}

}
