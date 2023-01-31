package org.ibp.api.java.role;

import org.generationcp.middleware.api.user.UserSearchRequest;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.RoleGeneratorInput;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

	long countRolesUsers(RoleSearchDto roleSearchDto);

	List<RoleDto> searchRoles(RoleSearchDto roleSearchDto, Pageable pageable);

	Integer createRole(RoleGeneratorInput dto);

	RoleDto getRole(Integer id);

	void updateRole(RoleGeneratorInput roleGeneratorInput);
}
