
package org.ibp.api.java.impl.middleware.security;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.study.StudySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public boolean isAccessible(StudySummary study) {
		if (!StringUtils.isBlank(study.getProgramUUID())) {
			Project studyProgram = this.workbenchDataManager.getProjectByUuid(study.getProgramUUID());
			List<User> allProgramMembers = this.workbenchDataManager.getUsersByProjectId(studyProgram.getProjectId());
			User loggedInUser = this.getCurrentlyLoggedInUser();
			return allProgramMembers.contains(loggedInUser);
		}
		// Blank program UUID == templates, allowed for all.
		return true;
	}

	@Override
	public User getCurrentlyLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new IllegalStateException("No authenticated user was found in security context.");
		}
		return this.workbenchDataManager.getUserByUsername(authentication.getName());
	}

}
