package org.ibp.api.java.impl.middleware.role.type;

import org.apache.commons.lang3.text.WordUtils;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.ibp.api.domain.role.RoleTypeDto;
import org.ibp.api.java.role.type.RoleTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleTypeServiceImpl implements RoleTypeService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<RoleTypeDto> getRoleTypes() {

		final List<RoleType> roleTypes = this.workbenchDataManager.getRoleTypes();
		final List<RoleTypeDto> roleTypeDtos = roleTypes.stream()
			.map(roleType -> new RoleTypeDto(roleType.getId(), WordUtils.capitalizeFully(roleType.getName())))
			.collect(Collectors.toList());
		return roleTypeDtos;
	}

}
