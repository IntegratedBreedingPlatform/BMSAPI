package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class StudyValidator {

	@Autowired
	private StudyDataManager studyDataManager;

	private BindingResult errors;

	public void validate(final Integer studyId, final Boolean shouldBeUnlocked) {

		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Study study = studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		if (shouldBeUnlocked && study.isLocked()) {
			errors.reject("study.is.locked", "");
			throw new ForbiddenException(errors.getAllErrors().get(0));
		}
	}

}
