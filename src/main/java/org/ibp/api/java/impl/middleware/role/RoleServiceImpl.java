package org.ibp.api.java.impl.middleware.role;

import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.java.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
	
	@Autowired
	private UserService userService;

	@Override
	public List<RoleDto> getAllRoles() {
		final List<RoleDto> roles = new ArrayList<>();
		
		final List<Role> assignableRoles = this.userService.getAllRoles();
		for (final Role role : assignableRoles) {
			roles.add(new RoleDto(role.getId(), role.getDescription()));
		}

		return roles;
	}

	@Override
	public List<RoleDto> getAssignableRoles() {
		final List<RoleDto> roles = new ArrayList<>();

		final List<Role> assignableRoles = this.userService.getAssignableRoles();
		for (final Role role : assignableRoles) {
			roles.add(new RoleDto(role.getId(), role.getDescription()));
		}

		return roles;
	}

}
