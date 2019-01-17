package org.ibp.api.java.impl.middleware.dataset.validator;

import org.apache.commons.lang3.ObjectUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Component
public class StudyValidator {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private ContextUtil contextUtil;

	private BindingResult errors;

	public void validate(final Integer studyId, final Boolean shouldBeUnlocked) {

		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Study study = studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

		if (shouldBeUnlocked
			&& study.isLocked()
			&& !ObjectUtils.equals(study.getCreatedBy(), String.valueOf(contextUtil.getIbdbUserId(loggedInUser.getUserid())))
			&& !request.isUserInRole(Role.SUPERADMIN)) {
			errors.reject("study.is.locked", "");
			throw new ForbiddenException(errors.getAllErrors().get(0));
		}
	}

}
