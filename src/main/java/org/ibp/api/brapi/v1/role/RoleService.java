package org.ibp.api.brapi.v1.role;

import java.util.List;

public interface RoleService {
	
	public List<RoleDto> getAllRoles();
	
	public List<RoleDto> getAssignableRoles();

}
