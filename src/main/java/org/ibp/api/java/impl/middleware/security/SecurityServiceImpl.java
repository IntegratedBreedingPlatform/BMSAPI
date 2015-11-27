
package org.ibp.api.java.impl.middleware.security;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
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

	@Autowired
	private UserDataManager userDataManager;

	@Override
	public boolean isAccessible(StudySummary study) {
		if (StringUtils.isBlank(study.getProgramUUID())) {
			// Blank program UUID == templates, allowed for all.
			return true;
		}
		return this.loggedInUserIsMemberOf(study.getProgramUUID());
	}

	@Override
	public boolean isAccessible(GermplasmList germplasmList) {

		if (StringUtils.isBlank(germplasmList.getProgramUUID())) {
			// Blank program UUID means this could be historic data loaded in crop db. Allow access to all such lists.
			return true;
		}

		// User reference on the gerplasmList is a reference to the User record in crop DB which is created by copying the User record in
		// Workbench db. The record ids might not be same but the user name must be the same.
		User cropDBListOwner = this.userDataManager.getUserById(germplasmList.getUserId());
		User workbenchListOwner = this.workbenchDataManager.getUserByUsername(cropDBListOwner.getName());
		User loggedInUser = this.getCurrentlyLoggedInUser();

		if (loggedInUser.equals(workbenchListOwner)) {
			return true;
		}

		return this.loggedInUserIsMemberOf(germplasmList.getProgramUUID());
	}

	private boolean loggedInUserIsMemberOf(String programUniqueId) {
		if (!StringUtils.isBlank(programUniqueId)) {
			User loggedInUser = this.getCurrentlyLoggedInUser();
			Project program = this.workbenchDataManager.getProjectByUuid(programUniqueId);
			List<User> allProgramMembers = this.workbenchDataManager.getUsersByProjectId(program.getProjectId());
			return allProgramMembers.contains(loggedInUser);
		}
		return false;
	}

	@Override
	public User getCurrentlyLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new IllegalStateException("No authenticated user was found in security context.");
		}
		return this.workbenchDataManager.getUserByUsername(authentication.getName());
	}

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}
}
