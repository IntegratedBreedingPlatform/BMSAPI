package org.ibp.api.java.role;

import org.ibp.api.domain.role.RoleDto;

import java.util.List;

public interface RoleService {
	
	public List<RoleDto> getAllRoles();
	
	public List<RoleDto> getAssignableRoles();

}
