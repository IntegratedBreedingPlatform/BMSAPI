package org.ibp.api.java.impl.middleware.role.type;

import org.generationcp.middleware.service.api.user.RoleTypeDto;
import org.ibp.api.java.role.type.RoleTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleTypeServiceImpl implements RoleTypeService {

	@Autowired
	private org.generationcp.middleware.api.role.RoleTypeService roleTypeService;

	@Override
	public List<RoleTypeDto> getRoleTypes() {
		return this.roleTypeService.getRoleTypes();
	}

}
