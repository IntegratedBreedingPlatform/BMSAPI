package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyTreeValidator;
import org.ibp.api.java.study.StudyTreeService;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	public ProgramValidator programValidator;

	@Resource
	private org.generationcp.middleware.service.api.study.StudyTreeService studyTreeService;

	@Override
	public Integer createStudyTreeFolder(final String cropName, final String programUUID, final Integer parentId, final String name) {
		this.studyTreeValidator.validateFolderName(name);

		final MapBindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.studyTreeValidator.validateFolderId(parentId);

		this.programValidator.validate(new ProgramDTO(cropName, programUUID), errors);
		if (errors.hasErrors()) {
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		this.studyTreeValidator.validateNotSameFolderNameInParent(name, parentId, programUUID);

		return this.studyTreeService.createStudyTreeFolder(parentId, name, programUUID);
	}

}
