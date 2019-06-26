package org.ibp.api.java.impl.middleware.role;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.java.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<RoleDto> getRoles(final RoleSearchDto roleSearchDto) {

		final List<Role> filteredRoles = this.workbenchDataManager.getRoles(roleSearchDto);
		final List<RoleDto> roles = filteredRoles.stream()
			.map(role -> new RoleDto(role))
			.collect(Collectors.toList());

		return roles;
	}
}
