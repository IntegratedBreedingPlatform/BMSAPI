
package org.ibp.api.java.impl.middleware.security;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudySummary;
import org.ibp.api.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.ObjectError;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService {

	public static final String CURRENT_USER_NOT_ADMIN_OR_SUPERADMIN = "current.user.not.admin.or.superadmin";

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private UserDataManager userDataManager;

	@Override
	public boolean isAccessible(final StudySummary study, final String cropname) {
		if (StringUtils.isBlank(study.getProgramUUID())) {
			// Blank program UUID == templates, allowed for all.
			return true;
		}
		return this.loggedInUserIsMemberOf(study.getProgramUUID(), cropname);
	}

	@Override
	public boolean isAccessible(final GermplasmList germplasmList, final String cropname) {

		if (StringUtils.isBlank(germplasmList.getProgramUUID())) {
			// Blank program UUID means this could be historic data loaded in crop db. Allow access to all such lists.
			return true;
		}

		// User reference on the gerplasmList is a reference to the User record in crop DB which is created by copying the User record in
		// Workbench db. The record ids might not be same but the user name must be the same.
		final User cropDBListOwner = this.userDataManager.getUserById(germplasmList.getUserId());
		final WorkbenchUser workbenchListOwner = this.workbenchDataManager.getUserByUsername(cropDBListOwner.getName());
		final WorkbenchUser loggedInUser = this.getCurrentlyLoggedInUser();

		if (loggedInUser.equals(workbenchListOwner)) {
			return true;
		}

		return this.loggedInUserIsMemberOf(germplasmList.getProgramUUID(), cropname);
	}

	@Override
	public void requireCurrentUserIsAdmin() {
		if (!this.request.isUserInRole(Role.ADMIN) && !this.request.isUserInRole(Role.SUPERADMIN)) {
			throw new ForbiddenException(
				new ObjectError("", new String[] {CURRENT_USER_NOT_ADMIN_OR_SUPERADMIN}, null, ""));
		}
	}

	private boolean loggedInUserIsMemberOf(final String programUniqueId, final String cropname) {
		if (!StringUtils.isBlank(programUniqueId)) {
			final WorkbenchUser loggedInUser = this.getCurrentlyLoggedInUser();
			final Project program = this.workbenchDataManager.getProjectByUuidAndCrop(programUniqueId, cropname);
			final List<WorkbenchUser> allProgramMembers = this.workbenchDataManager.getUsersByProjectId(program.getProjectId(), cropname);
			return allProgramMembers.contains(loggedInUser);
		}
		return false;
	}

	@Override
	public WorkbenchUser getCurrentlyLoggedInUser() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new IllegalStateException("No authenticated user was found in security context.");
		}
		return this.workbenchDataManager.getUserByUsername(authentication.getName());
	}

	public void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setUserDataManager(final UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}
}
