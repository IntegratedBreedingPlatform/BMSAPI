package org.ibp.api.java.impl.middleware.role;

import com.google.common.collect.Sets;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.RoleGeneratorInput;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.rest.role.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements org.ibp.api.java.role.RoleService {

	@Autowired
	private RoleValidator roleValidator;

	@Autowired
	private RoleService roleService;

	@Override
	public List<RoleDto> getRoles(final RoleSearchDto roleSearchDto) {
		return  this.roleService.getRoles(roleSearchDto);
	}

	@Override
	public Integer createRole(final RoleGeneratorInput dto) {

		final BindingResult errors = this.roleValidator.validateRoleGeneratorInput(dto, true);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final String userName = SecurityUtil.getLoggedInUserName();
		dto.setUsername(userName);
		final RoleDto roleDto = this.roleService.saveRole(dto);
		return roleDto.getId();
	}

	@Override
	public RoleDto getRole(final Integer id) {
		final Optional<RoleDto> role = this.roleService.getRoleWithUsers(id);
		if (!role.isPresent()) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("role.invalid.id");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		return role.get();
	}

	@Override
	public void updateRole(final RoleGeneratorInput roleGeneratorInput) {
		final BindingResult errors = this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final boolean isRoleInUse = this.roleService.isRoleInUse(roleGeneratorInput.getId());
		final RoleDto roleDto = this.roleService.getRoleById(roleGeneratorInput.getId()).get();

		// If the role is already assigned to any user and the role type has changed, throw an error.
		if (isRoleInUse && !roleDto.getRoleType().getId().equals(roleGeneratorInput.getRoleType())) {
			errors.reject("role.roletype.can.not.be.changed");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (roleGeneratorInput.isShowWarnings()) {
			final Set<Integer> rolePermissionIds =
				roleDto.getPermissions().stream().map(PermissionDto::getId).collect(Collectors.toSet());
			// If the role is assigned to any user and permissions have changed, throw a conflict error.
			if (isRoleInUse && !Sets.symmetricDifference(rolePermissionIds, Sets.newHashSet(roleGeneratorInput.getPermissions()))
				.isEmpty()) {
				errors.reject("role.permissions.changed", new Object[] {roleDto.getName()}, "");
				throw new ConflictException(errors.getAllErrors());
			}
		}

		this.roleService.updateRole(roleGeneratorInput);
	}
}
