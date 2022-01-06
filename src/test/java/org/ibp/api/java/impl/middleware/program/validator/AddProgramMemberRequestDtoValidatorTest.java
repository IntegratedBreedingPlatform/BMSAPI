package org.ibp.api.java.impl.middleware.program.validator;

import com.google.common.collect.Sets;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.workbench.AddProgramMemberRequestDto;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AddProgramMemberRequestDtoValidatorTest {

	private final String programUUID = RandomStringUtils.randomAlphabetic(16);
	private final Integer userId = RandomUtils.nextInt();
	private final Integer roleId = RandomUtils.nextInt();

	@Mock
	private UserService userService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	private AddProgramMemberRequestDtoValidator addProgramMemberRequestDtoValidator;

	@Test
	public void testValidate_ThrowsException_WhenRequestIsNull() {
		try {
			addProgramMemberRequestDtoValidator.validate(programUUID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenUserIdsIsEmpty() {
		try {
			final AddProgramMemberRequestDto addProgramMemberRequestDto = new AddProgramMemberRequestDto();
			addProgramMemberRequestDtoValidator.validate(programUUID, addProgramMemberRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.user.not.specified"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenRoleIdIsNull() {
		try {
			final AddProgramMemberRequestDto addProgramMemberRequestDto = new AddProgramMemberRequestDto();
			addProgramMemberRequestDto.setUserIds(Sets.newHashSet(userId));
			addProgramMemberRequestDtoValidator.validate(programUUID, addProgramMemberRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenUserIdsContainsNullValues() {
		try {
			final Set<Integer> set = new HashSet<>();
			set.add(null);
			final AddProgramMemberRequestDto addProgramMemberRequestDto = new AddProgramMemberRequestDto(roleId, set);
			addProgramMemberRequestDtoValidator.validate(programUUID, addProgramMemberRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.null.user.id"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenAnyUserDoesNotExist() {
		try {
			final AddProgramMemberRequestDto addProgramMemberRequestDto = new AddProgramMemberRequestDto(roleId, Sets.newHashSet(userId));
			Mockito.when(this.userService.getUsersByIds(Mockito.anyList())).thenReturn(Collections.emptyList());
			addProgramMemberRequestDtoValidator.validate(programUUID, addProgramMemberRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.user.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenRoleDoesNotExist() {
		try {
			final AddProgramMemberRequestDto addProgramMemberRequestDto = new AddProgramMemberRequestDto(roleId, Sets.newHashSet(userId));
			final WorkbenchUser user = new WorkbenchUser(userId);
			Mockito.when(this.userService.getUsersByIds(Mockito.anyList())).thenReturn(Collections.singletonList(user));
			Mockito.when(this.workbenchDataManager.getRoleById(Mockito.eq(roleId))).thenReturn(null);
			addProgramMemberRequestDtoValidator.validate(programUUID, addProgramMemberRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.role.do.not.exist"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenRoleDoesNotHaveProgramType() {
		try {
			final AddProgramMemberRequestDto addProgramMemberRequestDto = new AddProgramMemberRequestDto(roleId, Sets.newHashSet(userId));
			final WorkbenchUser user = new WorkbenchUser(userId);
			Mockito.when(this.userService.getUsersByIds(Mockito.anyList())).thenReturn(Collections.singletonList(user));
			final Role role = this.buildRole(roleId, org.generationcp.middleware.domain.workbench.RoleType.INSTANCE);
			Mockito.when(this.workbenchDataManager.getRoleById(Mockito.eq(roleId))).thenReturn(role);
			addProgramMemberRequestDtoValidator.validate(programUUID, addProgramMemberRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.invalid.user.role"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenUsersAreNotProgramMemberCandidates() {
		try {
			final AddProgramMemberRequestDto addProgramMemberRequestDto = new AddProgramMemberRequestDto(roleId, Sets.newHashSet(userId));
			final WorkbenchUser user = new WorkbenchUser(userId);
			final Role role = this.buildRole(roleId, org.generationcp.middleware.domain.workbench.RoleType.PROGRAM);
			Mockito.when(this.userService.getUsersByIds(Mockito.anyList())).thenReturn(Collections.singletonList(user));
			Mockito.when(this.workbenchDataManager.getRoleById(Mockito.eq(roleId))).thenReturn(role);
			Mockito.when(this.userService.getProgramMembersEligibleUsers(programUUID, null, null)).thenReturn(Collections.emptyList());
			addProgramMemberRequestDtoValidator.validate(programUUID, addProgramMemberRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.not.eligible.users"));
		}
	}

	private Role buildRole(final Integer id, final org.generationcp.middleware.domain.workbench.RoleType roleTypeEnum) {
		final Role role = new Role(id);
		final RoleType roleType = new RoleType();
		roleType.setId(roleTypeEnum.getId());
		role.setRoleType(roleType);
		return role;
	}
}
