package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyTreeValidator;
import org.ibp.api.java.study.StudyTreeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class StudyTreeServiceImpl implements StudyTreeService {

	@Resource
	private StudyTreeValidator studyTreeValidator;

	@Resource
	public ProgramValidator programValidator;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private org.generationcp.middleware.service.api.study.StudyTreeService studyTreeService;

	@Override
	public Integer createStudyTreeFolder(final String cropName, final String programUUID, final Integer parentId, final String folderName) {
		this.studyTreeValidator.validateFolderName(folderName);
		this.studyTreeValidator.validateFolderId(parentId, programUUID);
		this.validateProgram(cropName, programUUID);
		this.studyTreeValidator.validateNotSameFolderNameInParent(folderName, parentId, programUUID);

		return this.studyTreeService.createStudyTreeFolder(parentId, folderName, programUUID);
	}

	@Override
	public Integer updateStudyTreeFolder(final String cropName, final String programUUID, final Integer parentId, final String newFolderName) {
		this.studyTreeValidator.validateFolderName(newFolderName);
		final DmsProject folder = this.studyTreeValidator.validateFolderId(parentId, programUUID);

		//Preventing edition using the same folder name
		if (newFolderName.equalsIgnoreCase(folder.getName())) {
			return folder.getProjectId();
		}

		this.validateProgram(cropName, programUUID);
		this.studyTreeValidator.validateNotSameFolderNameInParent(newFolderName, folder.getParent().getProjectId(), programUUID);

		return this.studyTreeService.updateStudyTreeFolder(parentId, newFolderName);
	}

	@Override
	public void deleteStudyFolder(final String cropName, final String programUUID, final Integer folderId) {
		this.studyTreeValidator.validateFolderId(folderId, programUUID);
		this.validateProgram(cropName, programUUID);
		this.studyTreeValidator.validateFolderHasNoChildren(folderId, "study.folder.delete.has.child", programUUID);

		// TODO: should only the owner be able to delete the folder?

		this.studyTreeService.deleteStudyFolder(folderId);
	}

	@Override
	public TreeNode moveStudyFolder(final String cropName, final String programUUID, final Integer folderId,
		final Integer newParentFolderId) {
		if (folderId == null) {
			throw new ApiRequestValidationException("study.folder.id.invalid", new Object[] {});
		}

		if (newParentFolderId == null) {
			throw new ApiRequestValidationException("study.parent.folder.id.invalid", new Object[] {});
		}

		if (folderId.equals(newParentFolderId)) {
			throw new ApiRequestValidationException("study.folder.move.id.same.values", new Object[] {});
		}

		this.validateProgram(cropName, programUUID);
		final DmsProject folderToMove = this.studyTreeValidator.validateFolderId(folderId, programUUID);
		this.studyTreeValidator.validateFolderId(newParentFolderId, programUUID);

		this.studyTreeValidator.validateFolderHasNoChildren(folderId, "study.folder.move.has.child", programUUID);
		//Validate if there is a folder with same name in parent folder
		this.studyTreeValidator.validateNotSameFolderNameInParent(folderToMove.getName(), newParentFolderId, programUUID);

		final Integer movedFolderId = this.studyTreeService.moveStudyFolder(folderId, newParentFolderId);
		final List<Reference> folders = this.studyDataManager.getChildrenOfFolder(movedFolderId, programUUID);
		return TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true).get(0);
	}

	@Override
	public List<TreeNode> getStudyTree(final String parentKey, final String programUUID) {
		List<TreeNode> nodes = new ArrayList<>();
		if (StringUtils.isBlank(parentKey)) {
			final TreeNode rootNode = new TreeNode(DmsProject.SYSTEM_FOLDER_ID.toString(), AppConstants.STUDIES.getString(), true, null);
			nodes.add(rootNode);
		} else if (NumberUtils.isNumber(parentKey)) {
			final List<Reference> folders = this.studyDataManager.getChildrenOfFolder(Integer.valueOf(parentKey), programUUID);
			nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true);
		}
		return nodes;
	}

	private void validateProgram(final String cropName, final String programUUID) {
		final MapBindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.programValidator.validate(new ProgramDTO(cropName, programUUID), errors);
		if (errors.hasErrors()) {
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

}
