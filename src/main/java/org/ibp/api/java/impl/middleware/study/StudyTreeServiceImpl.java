package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyTreeValidator;
import org.ibp.api.java.study.StudyTreeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
@Transactional
public class StudyTreeServiceImpl implements StudyTreeService {

	@Resource
	private StudyTreeValidator studyTreeValidator;

	@Resource
	public ProgramValidator programValidator;

	@Resource
	private org.generationcp.middleware.service.api.study.StudyTreeService studyTreeService;

	@Override
	public Integer createStudyTreeFolder(final String cropName, final String programUUID, final Integer parentId, final String folderName) {
		this.studyTreeValidator.validateFolderName(folderName);
		this.studyTreeValidator.validateFolderId(parentId);
		this.validateProgram(cropName, programUUID);
		this.studyTreeValidator.validateNotSameFolderNameInParent(folderName, parentId, programUUID);

		return this.studyTreeService.createStudyTreeFolder(parentId, folderName, programUUID);
	}

	@Override
	public Integer updateStudyTreeFolder(final String cropName, final String programUUID, final int parentId, final String newFolderName) {
		this.studyTreeValidator.validateFolderName(newFolderName);
		final Study folder = this.studyTreeValidator.validateFolderId(parentId);

		//Preventing edition using the same folder name
		if (newFolderName.equalsIgnoreCase(folder.getName())) {
			return folder.getId();
		}

		this.validateProgram(cropName, programUUID);
		this.studyTreeValidator.validateNotSameFolderNameInParent(newFolderName, parentId, programUUID);

		return this.studyTreeService.updateStudyTreeFolder(parentId, newFolderName);
	}

	@Override
	public void deleteStudyFolder(final String cropName, final String programUUID, final Integer folderId) {
		this.studyTreeValidator.validateFolderId(folderId);
		this.validateProgram(cropName, programUUID);
		this.studyTreeValidator.validateFolderHasNoChildren(folderId, "study.delete.folder.has.child", programUUID);

		// TODO: should only the owner be able to delete the folder?

		this.studyTreeService.deleteStudyFolder(folderId);
	}

	private void validateProgram(final String cropName, final String programUUID) {
		final MapBindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.programValidator.validate(new ProgramDTO(cropName, programUUID), errors);
		if (errors.hasErrors()) {
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

}
