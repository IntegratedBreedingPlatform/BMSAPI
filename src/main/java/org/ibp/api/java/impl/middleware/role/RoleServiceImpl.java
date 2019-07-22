package org.ibp.api.java.impl.middleware.role;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.java.role.RoleService;
import org.ibp.api.rest.role.RoleGeneratorInput;
import org.ibp.api.rest.role.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleValidator roleValidator;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<RoleDto> getRoles(final RoleSearchDto roleSearchDto) {

		final List<Role> filteredRoles = this.workbenchDataManager.getRoles(roleSearchDto);
		final List<RoleDto> roles = filteredRoles.stream()
			.map(role -> new RoleDto(role))
			.collect(Collectors.toList());

		return roles;
	}

	@Override
	public void createRole(final RoleGeneratorInput dto) {
		//TODO Check by SITE_ADMIN authority

		this.roleValidator.validateRoleGeneratorInput(dto);

		final String userName = SecurityUtil.getLoggedInUserName();
		final WorkbenchUser user = this.workbenchDataManager.getUserByUsername(userName);
		final Role role  = new Role();
		role.setName(dto.getName());
		role.setEditable(dto.isEditable());
		role.setActive(dto.isActive());
		role.setAssignable(dto.isAssignable());
		role.setDescription(dto.getDescription());
		role.setCreatedDate(new Date());
		role.setCreatedBy(user);
		role.setActive(dto.isActive());
		role.setPermissions(this.getPermission(dto.getPermissions()));
		role.setRoleType(this.getRoleType(dto.getRoleType()));
		role.setUpdatedBy(user);
		role.setUpdatedDate(new Date());
		this.workbenchDataManager.saveOrUpdateRole(role);
	}

	private RoleType getRoleType(final Integer roleTypeId) {
		final RoleType roleType =  this.workbenchDataManager.getRoleType(roleTypeId);
		return roleType;
	}

	private List<Permission> getPermission(final List<Integer> permissions) {

		final List<Permission> permissionList = new ArrayList<>();
		for (final Integer permissionId : permissions) {
			final Permission permission = this.workbenchDataManager.getPermission(permissionId);
			permissionList.add(permission);

		}

		final Collection result = CollectionUtils.collect(permissionList, new Transformer() {
			@Override
			public Integer transform(final Object input) {
				final Permission permission = (Permission) input;
				return Integer.valueOf(permission.getPermissionId());
			}
		});

		for (final Iterator<Permission> it = permissionList.iterator(); it.hasNext();) {
			final Permission permission = it.next();
			if (result.contains(permission.getParent().getPermissionId())) {
				it.remove();
			}
		}

		return permissionList;
	}
}
