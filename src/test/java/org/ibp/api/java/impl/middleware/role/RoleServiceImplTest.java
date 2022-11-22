package org.ibp.api.java.impl.middleware.role;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.RoleGeneratorInput;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
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
		Mockito.doReturn(assignableRoles).when(this.roleService).getRoles(new RoleSearchDto(Boolean.TRUE, null, null));
	}

	@Test
	public void testUpdateRole() {

		final int roleId = 1;
		final int roleTypeId = 2;
		final int permissionId = 3;

		final Permission permission = new Permission();
		permission.setPermissionId(permissionId);

		final RoleDto role = new RoleDto(this.createTestRole(roleId, roleTypeId, new ArrayList<>()));

		final RoleGeneratorInput roleGeneratorInput =
			this.createRoleGeneratorInput(roleId, roleTypeId, Arrays.asList(permissionId));

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());

		when(this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false)).thenReturn(errors);
		when(this.roleService.getRoleById(roleGeneratorInput.getId())).thenReturn(Optional.of(role));
		when(this.roleService.isRoleInUse(roleGeneratorInput.getId())).thenReturn(false);

		when(this.permissionService.getPermissionsByIds(new HashSet<>(roleGeneratorInput.getPermissions())))
			.thenReturn(Arrays.asList(permission));

		this.roleServiceImpl.updateRole(roleGeneratorInput);

		final Optional<RoleDto> updatedRole = roleService.getRoleById(role.getId());

		verify(this.roleValidator).validateRoleGeneratorInput(roleGeneratorInput, false);
		assertEquals(roleGeneratorInput.getName(), updatedRole.get().getName());
		assertEquals(roleGeneratorInput.getDescription(), updatedRole.get().getDescription());
		assertEquals(permissionId, updatedRole.get().getPermissions().get(0).getId().intValue());
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

		final Permission permission = new Permission();
		permission.setPermissionId(permissionId);

		final Role role = this.createTestRole(roleId, 99, new ArrayList<>());

		final RoleGeneratorInput roleGeneratorInput =
			this.createRoleGeneratorInput(roleId, roleTypeId, Arrays.asList(permissionId));

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());

		when(this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false)).thenReturn(errors);
		when(this.roleService.getRoleById(roleGeneratorInput.getId())).thenReturn(Optional.of(new RoleDto(role)));
		when(this.roleService.isRoleInUse(roleGeneratorInput.getId())).thenReturn(true);

		when(this.permissionService.getPermissionsByIds(new HashSet<>(roleGeneratorInput.getPermissions())))
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

		final Permission permission1 = new Permission();
		permission1.setPermissionId(permissionId1);
		final Permission permission2 = new Permission();
		permission2.setPermissionId(permissionId2);

		// Role to be retrieved from the database has one permission
		final Role role = this.createTestRole(roleId, roleTypeId, Arrays.asList(permission1));

		// Add new permission to the role.
		final RoleGeneratorInput roleGeneratorInput =
			this.createRoleGeneratorInput(roleId, roleTypeId, Arrays.asList(permissionId1, permissionId2));

		// Set to true, to tell the system to detect permissions changes
		roleGeneratorInput.setShowWarnings(true);

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), RoleGeneratorInput.class.getName());

		when(this.roleValidator.validateRoleGeneratorInput(roleGeneratorInput, false)).thenReturn(errors);
		when(this.roleService.getRoleById(roleGeneratorInput.getId())).thenReturn(Optional.of(new RoleDto(role)));
		when(this.roleService.isRoleInUse(roleGeneratorInput.getId())).thenReturn(true);
		when(this.permissionService.getPermissionsByIds(new HashSet<>(roleGeneratorInput.getPermissions())))
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

	private Role createTestRole(final int roleId, final int roleTypeId, final List<Permission> pemissions) {
		final Role role = new Role();
		role.setId(roleId);
		role.setPermissions(pemissions);
		final RoleType roleType = new RoleType();
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
