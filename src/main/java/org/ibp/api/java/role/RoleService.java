package org.ibp.api.java.role;

import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.RoleGeneratorInput;
import org.generationcp.middleware.service.api.user.RoleSearchDto;

import java.util.List;

public interface RoleService {

	List<RoleDto> getRoles(RoleSearchDto roleSearchDto);

	Integer createRole(RoleGeneratorInput dto);

	RoleDto getRole(Integer id);

	void updateRole(RoleGeneratorInput roleGeneratorInput);
}
