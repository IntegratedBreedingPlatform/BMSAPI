package org.ibp.api.java.impl.middleware.role;

import com.google.common.collect.Sets;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.java.role.RoleService;
import org.ibp.api.rest.role.RoleGeneratorInput;
import org.ibp.api.rest.role.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

	public static final String ROLE_ASSIGNED_TO_USER_WARNING_KEY = "roleAssignedToUserWarning";
	public static final String PERMISSION_CHANGED_WARNING_KEY = "permissionChangedWarning";
	@Autowired
	private RoleValidator roleValidator;

	@Autowired
	private UserService userService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Override
	public List<RoleDto> getRoles(final RoleSearchDto roleSearchDto) {

		final List<Role> filteredRoles = this.workbenchDataManager.getRoles(roleSearchDto);
		final List<RoleDto> roles = filteredRoles.stream()
			.map(role -> new RoleDto(role))
			.collect(Collectors.toList());

		return roles;
	}

	@Override
	public Integer createRole(final RoleGeneratorInput dto) {

		final BindingResult errors = this.roleValidator.validateRoleGeneratorInput(dto, true);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final String userName = SecurityUtil.getLoggedInUserName();
		final WorkbenchUser user = this.userService.getUserByUsername(userName);
		final Role role = new Role();
		role.setName(dto.getName());
		role.setEditable(dto.isEditable());
		role.setAssignable(dto.isAssignable());
		role.setDescription(dto.getDescription());
		role.setCreatedDate(new Date());
		role.setCreatedBy(user);
		role.setActive(true);
		role.setPermissions(this.getPermission(dto.getPermissions()));
		role.setRoleType(this.workbenchDataManager.getRoleType(dto.getRoleType()));
		role.setUpdatedBy(user);
		role.setUpdatedDate(new Date());
		this.workbenchDataManager.saveRole(role);
		return role.getId();
	}

	@Override
	public RoleDto getRole(final Integer id) {
		final Role role = this.workbenchDataManager.getRoleById(id);
		if (role == null) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("role.invalid.id");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		return new RoleDto(role);
	}

	@Override
	public void updateRole(final RoleGeneratorInput roleGeneratorInput) {
		final BindingResult errors = this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Role role = this.workbenchDataManager.getRoleById(roleGeneratorInput.getId());
		final List<WorkbenchUser> roleUsers = this.userService.getUsersWithRole(roleGeneratorInput.getId());

		// If the role is already assigned to any user and the role type has changed, throw an error.
		if (!roleUsers.isEmpty() && !role.getRoleType().getId().equals(roleGeneratorInput.getRoleType())) {
			errors.reject("role.roletype.can.not.be.changed");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (roleGeneratorInput.isShowWarnings()) {
			final Set<Integer> rolePermissionIds =
				role.getPermissions().stream().map(Permission::getPermissionId).collect(Collectors.toSet());
			// If the role is assigned to any user and permissions have changed, throw a conflict error.
			if (!roleUsers.isEmpty() && !Sets.symmetricDifference(rolePermissionIds, Sets.newHashSet(roleGeneratorInput.getPermissions()))
				.isEmpty()) {
				errors.reject("role.permissions.changed", new Object[] {role.getName()}, "");
				throw new ConflictException(errors.getAllErrors());
			}
		}

		role.setName(roleGeneratorInput.getName());
		role.setDescription(roleGeneratorInput.getDescription());
		role.setPermissions(this.permissionService.getPermissionsByIds(Sets.newHashSet(roleGeneratorInput.getPermissions())));
		this.workbenchDataManager.saveRole(role);
	}

	private List<Permission> getPermission(final List<Integer> permissions) {

		final List<Permission> permissionList = this.permissionService.getPermissionsByIds(new HashSet<>(permissions));

		for (final Iterator<Permission> it = permissionList.iterator(); it.hasNext(); ) {
			final Permission permission = it.next();
			if (permission.getParent() != null && permissions.contains(permission.getParent().getPermissionId())) {
				it.remove();
			}
		}

		return permissionList;
	}

}
