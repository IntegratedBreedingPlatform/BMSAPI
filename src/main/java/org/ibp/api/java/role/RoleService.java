package org.ibp.api.java.role;

import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.rest.role.RoleGeneratorInput;

import java.util.List;

public interface RoleService {

	List<RoleDto> getRoles(RoleSearchDto roleSearchDto);

	void createRole(RoleGeneratorInput dto);
}
