package org.ibp.api.java.impl.middleware.permission;

import org.generationcp.middleware.api.role.RoleTypeService;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.permission.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Service
public class PermissionServiceImpl implements PermissionService {

	@Autowired
	private RoleTypeService roleTypeService;

	@Autowired
	private org.generationcp.middleware.service.api.permission.PermissionService permissionService;

	@Override
	public PermissionDto getPermissionTree(final Integer roleTypeId) {

		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "roleTypeId");

		if (roleTypeId == null) {
			errors.reject("role.type.can.not.be.null");

		}

		final RoleType roleType = roleTypeService.getRoleType(roleTypeId);
		if (roleType == null) {
			errors.reject("role.role.type.does.not.exist");
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final PermissionDto permissionDto = permissionService.getPermissionTree(roleTypeId);

		return permissionDto;
	}
}
