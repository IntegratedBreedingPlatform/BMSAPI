package org.ibp.api.java.impl.middleware.role;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.RoleGeneratorInput;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.generationcp.middleware.service.api.user.RoleTypeDto;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.rest.role.RoleValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleServiceImplTest extends ApiUnitTestBase {

	public static final int RANDOM_COUNT = 10;

	@Mock
	private PermissionService permissionService;

	@Mock
	private RoleValidator roleValidator;

	@Mock
	private RoleService roleService;

	@InjectMocks
	private RoleServiceImpl roleServiceImpl;

	private List<Role> allRoles;
	private Role restrictedRole;

	@Before
	public void setup() {
		this.createTestRoles();
		final List<Role> assignableRoles = new ArrayList<>(this.allRoles);
		assignableRoles.remove(this.restrictedRole);
		Mockito.doReturn(assignableRoles).when(this.roleService).searchRoles(new RoleSearchDto(Boolean.TRUE, null, null), null);
	}

	@Test
	public void testUpdateRole() {

		final int roleId = 1;
		final int roleTypeId = 2;
		final int permissionId = 3;

		final PermissionDto permission = new PermissionDto();
		permission.setId(permissionId);

		final RoleDto role = this.createRoleDto(roleId, roleTypeId, new ArrayList<>());

		final RoleGeneratorInput roleGeneratorInput =
			this.createRoleGeneratorInput(roleId, roleTypeId, Arrays.asList(permissionId));

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());

		when(this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false)).thenReturn(errors);
		when(this.roleService.getRoleById(roleGeneratorInput.getId())).thenReturn(Optional.of(role));
		when(this.roleService.isRoleInUse(roleGeneratorInput.getId())).thenReturn(false);

		this.roleServiceImpl.updateRole(roleGeneratorInput);
		verify(this.roleValidator).validateRoleGeneratorInput(roleGeneratorInput, false);
		verify(this.roleService).updateRole(any());
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testUpdateRole_RoleGeneratorInputValidationError() {

		final int roleId = 1;
		final int roleTypeId = 2;
		final RoleGeneratorInput roleGeneratorInput =
			this.createRoleGeneratorInput(roleId, roleTypeId, new ArrayList<>());

		final BindingResult errors = Mockito.mock(BindingResult.class);
		when(errors.hasErrors()).thenReturn(true);
		when(this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false)).thenReturn(errors);

		this.roleServiceImpl.updateRole(roleGeneratorInput);

	}

	@Test
	public void testUpdateRole_RoleTypeIsChanged() {

		final int roleId = 1;
		final int roleTypeId = 2;
		final int permissionId = 3;

		final PermissionDto permission = new PermissionDto();
		permission.setId(permissionId);

		final RoleDto role = this.createRoleDto(roleId, 99, new ArrayList<>());

		final RoleGeneratorInput roleGeneratorInput =
			this.createRoleGeneratorInput(roleId, roleTypeId, Arrays.asList(permissionId));

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());

		when(this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false)).thenReturn(errors);
		when(this.roleService.getRoleById(roleGeneratorInput.getId())).thenReturn(Optional.of(role));
		when(this.roleService.isRoleInUse(roleGeneratorInput.getId())).thenReturn(true);

		when(this.permissionService.getPermissionsDtoByIds(new HashSet<>(roleGeneratorInput.getPermissions())))
			.thenReturn(Arrays.asList(permission));

		try {
			this.roleServiceImpl.updateRole(roleGeneratorInput);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			// do nothing
		}
		verify(this.roleValidator).validateRoleGeneratorInput(roleGeneratorInput, false);
		assertEquals("role.roletype.can.not.be.changed", errors.getAllErrors().get(0).getCode());
	}

	@Test
	public void testUpdateRole_PermissionsAreChanged() {

		final int roleId = 1;
		final int roleTypeId = 2;
		final int permissionId1 = 99;
		final int permissionId2 = 100;

		final PermissionDto permission1 = new PermissionDto();
		permission1.setId(permissionId1);
		final PermissionDto permission2 = new PermissionDto();
		permission2.setId(permissionId2);

		// Role to be retrieved from the database has one permission
		final RoleDto role = this.createRoleDto(roleId, roleTypeId, Arrays.asList(permission1));

		// Add new permission to the role.
		final RoleGeneratorInput roleGeneratorInput =
			this.createRoleGeneratorInput(roleId, roleTypeId, Arrays.asList(permissionId1, permissionId2));

		// Set to true, to tell the system to detect permissions changes
		roleGeneratorInput.setShowWarnings(true);

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());

		when(this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false)).thenReturn(errors);
		when(this.roleService.getRoleById(roleGeneratorInput.getId())).thenReturn(Optional.of(role));
		when(this.roleService.isRoleInUse(roleGeneratorInput.getId())).thenReturn(true);
		when(this.permissionService.getPermissionsDtoByIds(new HashSet<>(roleGeneratorInput.getPermissions())))
			.thenReturn(Arrays.asList(permission1, permission2));

		try {
			this.roleServiceImpl.updateRole(roleGeneratorInput);
			fail("Method should throw an error");
		} catch (final ConflictException e) {
			// do nothing
		}
		verify(this.roleValidator).validateRoleGeneratorInput(roleGeneratorInput, false);
		assertEquals("role.permissions.changed", errors.getAllErrors().get(0).getCode());
	}

	private RoleDto createRoleDto(final int roleId, final int roleTypeId, final List<PermissionDto> pemissions) {
		final RoleDto role = new RoleDto();
		role.setId(roleId);
		role.setPermissions(pemissions);
		final RoleTypeDto roleType = new RoleTypeDto();
		roleType.setId(roleTypeId);
		role.setRoleType(roleType);
		return role;
	}

	private RoleGeneratorInput createRoleGeneratorInput(final int roleId, final int roleTypeId,
		final List<Integer> permissionIds) {
		final String roleName = RandomStringUtils.randomAlphabetic(RANDOM_COUNT);
		final String description = RandomStringUtils.randomAlphabetic(RANDOM_COUNT);
		final RoleGeneratorInput roleGeneratorInput = new RoleGeneratorInput();
		roleGeneratorInput.setId(roleId);
		roleGeneratorInput.setName(roleName);
		roleGeneratorInput.setDescription(description);
		roleGeneratorInput.setShowWarnings(false);
		roleGeneratorInput.setPermissions(permissionIds);
		roleGeneratorInput.setRoleType(roleTypeId);
		return roleGeneratorInput;
	}

	private void createTestRoles() {
		this.allRoles = new ArrayList<>();
		final Role admin = new Role(1, "ADMIN");
		final Role breeder = new Role(2, "BREEDER");
		final Role technician = new Role(3, "TECHNICIAN");
		this.restrictedRole = new Role(4, Role.SUPERADMIN);

		this.allRoles.add(admin);
		this.allRoles.add(breeder);
		this.allRoles.add(technician);
		this.allRoles.add(this.restrictedRole);
	}
}
