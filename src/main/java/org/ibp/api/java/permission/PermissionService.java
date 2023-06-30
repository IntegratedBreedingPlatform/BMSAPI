package org.ibp.api.java.permission;

import org.generationcp.middleware.domain.workbench.PermissionDto;

import java.util.List;

public interface PermissionService {

	PermissionDto getPermissionTree(Integer roleTypeId);

	List<PermissionDto> getPermissions(Integer userId, String cropName, Integer programId, Boolean skipProgramValidation);
}
