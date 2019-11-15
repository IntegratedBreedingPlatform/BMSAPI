package org.ibp.api.java.permission;

import org.generationcp.middleware.domain.workbench.PermissionDto;

public interface PermissionService {

	PermissionDto getPermissionTree(Integer roleTypeId);

}
