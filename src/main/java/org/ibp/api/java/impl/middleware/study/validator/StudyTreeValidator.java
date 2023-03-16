package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class StudyTreeValidator {

	public static final int NAME_MAX_LENGTH = 255;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private StudyService studyService;

	public void validateFolderName(final String folderName) {
		if (StringUtils.isEmpty(folderName)) {
			throw new ApiRequestValidationException("study.folder.empty", new Object[] {});
		}

		if (folderName.length() > NAME_MAX_LENGTH) {
			throw new ApiRequestValidationException("study.folder.name.too.long", new Object[] {NAME_MAX_LENGTH});
		}
	}

	public DmsProject validateNodeId(final Integer nodeId, final String programUUID) {
		final DmsProject project = this.studyDataManager.getProject(nodeId);
		if (project == null || (project.getProgramUUID() != null && !project.getProgramUUID().equals(programUUID))) {
			throw new ApiRequestValidationException("study.node.id.not.exist", new Object[] {nodeId});
		}
		return project;
	}

	public DmsProject validateFolderId(final Integer folderId, final String programUUID) {
		final DmsProject project = this.studyDataManager.getProject(folderId);
		if (project == null || !project.isFolder() || (project.getProgramUUID() != null && !project.getProgramUUID().equals(programUUID))) {
			throw new ApiRequestValidationException("study.folder.id.not.exist", new Object[] {folderId});
		}
		return project;
	}

	public void validateNotSameFolderNameInParent(final String folderName, final Integer parentId, final String programUUID) {
		this.studyService.getFolderByParentAndName(parentId, folderName, programUUID)
			.ifPresent(germplasmList -> {
				throw new ApiRequestValidationException("study.folder.name.exists", new Object[] {folderName});
			});
	}

	public void validateFolderHasNoChildren(final Integer folderId, final String message, final String programUUID) {
		final List<Reference> children = this.studyDataManager.getChildrenOfFolder(folderId, programUUID);
		if (!CollectionUtils.isEmpty(children)) {
			throw new ApiRequestValidationException(message, new Object[] {folderId.toString()});
		}
	}

}