
package org.ibp.api.java.impl.middleware.security;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private UserService userService;

	@Override
	public boolean isAccessible(final GermplasmList germplasmList, final String cropname) {

		if (StringUtils.isBlank(germplasmList.getProgramUUID())) {
			// Blank program UUID means this could be historic data loaded in crop db. Allow access to all such lists.
			return true;
		}

		final WorkbenchUser listOwner = this.userService.getUserById(germplasmList.getUserId());
		final WorkbenchUser loggedInUser = this.getCurrentlyLoggedInUser();

		if (loggedInUser.equals(listOwner)) {
			return true;
		}

		return this.loggedInUserIsMemberOf(germplasmList.getProgramUUID(), cropname);
	}

	private boolean loggedInUserIsMemberOf(final String programUniqueId, final String cropname) {
		if (!StringUtils.isBlank(programUniqueId)) {
			final WorkbenchUser loggedInUser = this.getCurrentlyLoggedInUser();
			final Project program = this.workbenchDataManager.getProjectByUuidAndCrop(programUniqueId, cropname);
			final List<WorkbenchUser> allProgramMembers = this.userService.getUsersByProjectId(program.getProjectId());
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
		return this.userService.getUserByUsername(authentication.getName());
	}

	public void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

}
