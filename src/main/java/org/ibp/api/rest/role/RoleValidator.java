package org.ibp.api.rest.role;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.api.role.RoleTypeService;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.RoleGeneratorInput;
import org.generationcp.middleware.service.api.user.RoleTypeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleValidator {

	public static final String ROLE_NAME_FIELD = "roleName";
	public static final String ROLE_TYPE_FIELD = "roleType";
	public static final String ROLE_DESCRIPTION_FIELD = "roleDescription";

	@Autowired
	private RoleService roleService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RoleTypeService roleTypeService;

	public BindingResult validateRoleGeneratorInput(final RoleGeneratorInput roleGeneratorInput, final boolean isNewRole) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());

		if (StringUtils.isBlank(roleGeneratorInput.getName())) {
			errors.rejectValue(ROLE_NAME_FIELD, "role.name.can.not.be.null.empty");
			return errors;
		}

		if (roleGeneratorInput.getRoleType() == null) {
			errors.rejectValue(ROLE_TYPE_FIELD, "role.type.can.not.be.null");
			return errors;
		}

		if (roleGeneratorInput.getName().length() > 100) {
			errors.rejectValue(ROLE_NAME_FIELD, "role.name.length");
		}

		if (isNewRole && this.roleService.getRoleByName(roleGeneratorInput.getName()).isPresent()) {
			errors.rejectValue(ROLE_NAME_FIELD, "role.name.already.exists");
		}

		if (roleGeneratorInput.getDescription() != null && roleGeneratorInput.getDescription().length() > 255) {
			errors.rejectValue(ROLE_DESCRIPTION_FIELD, "role.description.length");
		}

		final RoleTypeDto roleType = this.roleTypeService.getRoleType(roleGeneratorInput.getRoleType());
		if (roleType == null) {
			errors.reject("role.role.type.does.not.exist");
		} else {
			final Set<Integer> permissionsIdSet = new HashSet<>(roleGeneratorInput.getPermissions());
			final List<PermissionDto> permissionDtoList = this.permissionService.getPermissionsDtoByIds(permissionsIdSet);
			if (permissionDtoList.size() != permissionsIdSet.size()) {
				permissionsIdSet.remove(permissionDtoList.stream().map(a -> a.getId()).collect(Collectors.toSet()));
				errors.reject("role.permission.does.not.exist",
					new String[] {permissionsIdSet.stream().map(a -> a.toString()).collect(Collectors.joining(" , "))},
					"");
			} else {
				for (final PermissionDto permission: permissionDtoList) {
					if (!permission.getRoleTypeSelectableMap().containsKey(roleType)) {
						errors.rejectValue(ROLE_TYPE_FIELD, "role.role.type.does.not.correspond");
						break;
					}
				}

				for (final PermissionDto permission: permissionDtoList) {
					if (permission.getRoleTypeSelectableMap().containsKey(roleType) && !permission.getRoleTypeSelectableMap().get(roleType)) {
						errors.reject("role.permission.not.selectable");
						break;
					}
				}
			}
		}

		if (roleGeneratorInput.isAssignable() == null) {
			errors.reject("role.assignable.null");
		}

		if (roleGeneratorInput.isEditable() == null) {
			errors.reject("role.editable.null");
		}

		return  errors;

	}
}
