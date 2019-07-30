package org.ibp.api.java.impl.middleware.role;

import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.role.RoleService;
import org.ibp.api.rest.role.RoleGeneratorInput;
import org.ibp.api.rest.role.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleValidator roleValidator;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private PermissionService permissionService;

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

		BindingResult errors = this.roleValidator.validateRoleGeneratorInput(dto);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final String userName = SecurityUtil.getLoggedInUserName();
		final WorkbenchUser user = this.workbenchDataManager.getUserByUsername(userName);
		final Role role  = new Role();
		role.setName(dto.getName());
		role.setEditable(dto.isEditable());
		role.setAssignable(dto.isAssignable());
		role.setDescription(dto.getDescription());
		role.setCreatedDate(new Date());
		role.setCreatedBy(user);
		role.setActive(true);
		role.setPermissions(this.getPermission(dto.getPermissions()));
		role.setRoleType(this.getRoleType(dto.getRoleType()));
		role.setUpdatedBy(user);
		role.setUpdatedDate(new Date());
		this.workbenchDataManager.saveRole(role);
		return role.getId();
	}

	private RoleType getRoleType(final Integer roleTypeId) {
		final RoleType roleType =  this.workbenchDataManager.getRoleType(roleTypeId);
		return roleType;
	}

	private List<Permission> getPermission(final List<Integer> permissions) {

		final List<Permission> permissionList = this.permissionService.getPermissionsByIds(new HashSet<>(permissions));

		for (final Iterator<Permission> it = permissionList.iterator(); it.hasNext();) {
			final Permission permission = it.next();
			if (permission.getParent() != null && permissions.contains(permission.getParent().getPermissionId())) {
				it.remove();
			}
		}

		return permissionList;
	}

}
