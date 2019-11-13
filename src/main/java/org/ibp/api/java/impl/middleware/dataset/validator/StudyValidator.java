package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class StudyValidator {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService middlewareStudyService;

	private BindingResult errors;

	public void validate(final Integer studyId, final Boolean shouldBeUnlocked) {

		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (studyId == null) {
			this.errors.reject("study.required", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Study study = studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

		if (shouldBeUnlocked
			&& study.isLocked()
			&& !study.getCreatedBy().equals(loggedInUser.getUserid().toString())
			&& !loggedInUser.isSuperAdmin()) {
			errors.reject("study.is.locked", "");
			throw new ForbiddenException(errors.getAllErrors().get(0));
		}
	}

	public void validate(final Integer studyId, final Boolean shouldBeUnlocked, final Boolean canHaveAdvanceOrCrossList) {
		this.validate(studyId, shouldBeUnlocked);

		if (!canHaveAdvanceOrCrossList && this.middlewareStudyService.hasAdvancedOrCrossesList(studyId)) {
			this.errors.reject("study.has.advance.or.cross.list");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
