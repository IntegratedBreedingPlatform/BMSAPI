package org.ibp.api.java.impl.middleware.role;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.java.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<RoleDto> getAllRoles() {
		final List<RoleDto> roles = new ArrayList<RoleDto>();
		
		final List<Role> assignableRoles = this.workbenchDataManager.getAllRoles();
		for (final Role role : assignableRoles) {
			roles.add(new RoleDto(role.getId(), role.getName()));
		}

		return roles;
	}

	@Override
	public List<RoleDto> getAssignableRoles() {
		final List<RoleDto> roles = new ArrayList<RoleDto>();
		
		final List<Role> assignableRoles = this.workbenchDataManager.getAssignableRoles();
		for (final Role role : assignableRoles) {
			roles.add(new RoleDto(role.getId(), role.getCapitalizedRole()));
		}

		return roles;
	}

}
