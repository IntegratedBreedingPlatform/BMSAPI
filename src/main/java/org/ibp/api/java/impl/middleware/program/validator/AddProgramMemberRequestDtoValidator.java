package org.ibp.api.java.impl.middleware.program.validator;

import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.domain.workbench.AddProgramMemberRequestDto;
import org.generationcp.middleware.domain.workbench.RoleType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AddProgramMemberRequestDtoValidator {

	@Autowired
	private UserService userService;

	@Autowired
	@Lazy
	private RoleService roleService;

	public void validate(final String programUUID, final AddProgramMemberRequestDto addProgramMemberRequestDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), AddProgramMemberRequestDto.class.getName());

		//request is not null
		BaseValidator.checkNotNull(addProgramMemberRequestDto, "param.null", new String[] {"request body"});

		//at least one user is specified
		BaseValidator.checkNotEmpty(addProgramMemberRequestDto.getUserIds(), "program.member.user.not.specified");

		//role is specified
		BaseValidator.checkNotNull(addProgramMemberRequestDto.getRoleId(), "param.null", new String[] {"roleId"});

		//check that any userId is null
		if (addProgramMemberRequestDto.getUserIds().stream().anyMatch(Objects::isNull)) {
			errors.reject("program.member.null.user.id", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		//all users exists
		final List<Integer> existingUserIds =
			userService.getUsersByIds(new ArrayList<>(addProgramMemberRequestDto.getUserIds())).stream().map(WorkbenchUser::getUserid)
				.collect(Collectors.toList());
		if (existingUserIds.size() != addProgramMemberRequestDto.getUserIds().size()) {
			final List<Integer> invalidUsers = new ArrayList<>(addProgramMemberRequestDto.getUserIds());
			invalidUsers.removeAll(existingUserIds);
			errors.reject("program.member.user.invalid", new String[] {Util.buildErrorMessageFromList(invalidUsers, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		//role is a program role
		final Optional<RoleDto> role = this.roleService.getRoleById(addProgramMemberRequestDto.getRoleId());
		if (!role.isPresent()) {
			errors.reject("program.member.role.do.not.exist", new String[] {String.valueOf(addProgramMemberRequestDto.getRoleId())}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (!role.get().getRoleType().getId().equals(RoleType.PROGRAM.getId())) {
			errors.reject("program.member.invalid.user.role", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		//all users are eligible to be program members
		final List<Integer> eligibleUserIds =
			this.userService.getProgramMembersEligibleUsers(programUUID, null, null).stream().map(UserDto::getId)
				.collect(Collectors.toList());
		if (!eligibleUserIds.containsAll(addProgramMemberRequestDto.getUserIds())) {
			final List<Integer> invalidUsers = new ArrayList<>(addProgramMemberRequestDto.getUserIds());
			invalidUsers.removeAll(eligibleUserIds);
			errors.reject("program.member.not.eligible.users", new String[] {Util.buildErrorMessageFromList(invalidUsers, 3), programUUID},
				"");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
