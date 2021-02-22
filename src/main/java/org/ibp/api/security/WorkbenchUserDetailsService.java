package org.ibp.api.security;

import liquibase.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.impl.middleware.common.ContextResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// **Important note for developers** : This class is central to the authentication framework of BMSAPI. Please do not alter it without a
// good understanding of Spring Security in general and BMS X-Auth-Token based authentication workflow in particular, otherwise there will
// be MAJOR breakages in the functioning of BMS components. Consult your friendly senior developer first if you are unsure.
@Component
public class WorkbenchUserDetailsService implements UserDetailsService {

	@Autowired
	private ContextResolver contextResolver;

	@Autowired
	private UserService userService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private CropService cropService;

	public WorkbenchUserDetailsService() {

	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			// username must be converted from html-encode to utf-8 string to support chinese/utf-8 languages
			username = StringEscapeUtils.unescapeHtml(username);

			final List<WorkbenchUser> matchingUsers = this.userService.getUserByName(username, 0, 1, Operation.EQUAL);
			if (matchingUsers != null && !matchingUsers.isEmpty()) {
				final WorkbenchUser workbenchUser = matchingUsers.get(0);
				// FIXME Populate flags for accountNonExpired, credentialsNonExpired, accountNonLocked properly, all true for now.
				return new org.springframework.security.core.userdetails.User(workbenchUser.getName(), workbenchUser.getPassword(),
						this.getAuthorities(workbenchUser));
			}
			throw new UsernameNotFoundException("Invalid username/password.");
		} catch (final MiddlewareQueryException e) {
			throw new AuthenticationServiceException("Data access error while authenticating user against Workbench.", e);
		} catch (final AccessDeniedException e) {
			throw new AuthenticationServiceException("Access Denied: " + e.getMessage(), e);
		}
	}

	private Collection<? extends GrantedAuthority> getAuthorities(final WorkbenchUser workbenchUser) throws AccessDeniedException {
		final String cropName = this.contextResolver.resolveCropNameFromUrl();

		final String programUUID = this.contextResolver.resolveProgramUuidFromRequest();
		final Integer programId = StringUtils.isEmpty(programUUID) ? null : this.getProgramId(programUUID);

		final List<PermissionDto> permissions = this.permissionService.getPermissions( //
			workbenchUser.getUserid(), //
			StringUtils.isEmpty(cropName) ? null : cropName, //
			programId);

		// Skip crop authorization checking if the user has Site Admin Permission
		if(!StringUtils.isEmpty(cropName) && !hasSiteAdminPermissions(permissions)) {
			final List<String> crops = this.cropService.getAvailableCropsForUser(workbenchUser.getUserid());
			crops.replaceAll(String::toUpperCase);
			if(!crops.contains(cropName.trim().toUpperCase())) {
				throw new AccessDeniedException("User is not authorized for crop.");
			}
		}

		//Required because not all our REST services that receives programUUID has a PreAuthorize control
		if (programId != null && !workbenchUser.hasAccessToAGivenProgram(cropName, Long.valueOf(programId))) {
			throw new AccessDeniedException("User is not authorized for the program.");
		}

		return permissions.stream().map(permissionDto -> new SimpleGrantedAuthority(permissionDto.getName())).collect(
				Collectors.toCollection(ArrayList::new));
	}

	private boolean hasSiteAdminPermissions(final List<PermissionDto> permissions) {
		final List<String> permissionString = permissions.stream().map(permission -> permission.getName()).collect(Collectors.toList());
		return CollectionUtils.containsAny(permissionString, PermissionsEnum.SITE_ADMIN_PERMISSIONS);
	}

	private Integer getProgramId(final String programUUID) {
		final Project project = this.workbenchDataManager.getProjectByUuid(programUUID);
		return project != null ? project.getProjectId().intValue() : null;
	}

}
