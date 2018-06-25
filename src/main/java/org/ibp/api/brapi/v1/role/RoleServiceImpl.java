package org.ibp.api.brapi.v1.role;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<RoleDto> getAllRoles() {
		List<RoleDto> roles = new ArrayList<RoleDto>();
		
		final List<Role> assignableRoles = this.workbenchDataManager.getAllRoles();
		for (final Role role : assignableRoles) {
			roles.add(new RoleDto(role.getId(), role.getDescription()));
		}

		return roles;
	}

	@Override
	public List<RoleDto> getAssignableRoles() {
		List<RoleDto> roles = new ArrayList<RoleDto>();
		
		final List<Role> assignableRoles = this.workbenchDataManager.getAssignableRoles();
		for (final Role role : assignableRoles) {
			roles.add(new RoleDto(role.getId(), role.getDescription()));
		}

		return roles;
	}

}
