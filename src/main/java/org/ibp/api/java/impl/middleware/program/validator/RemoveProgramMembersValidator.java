package org.ibp.api.java.impl.middleware.program.validator;

import org.generationcp.middleware.domain.workbench.ProgramMemberDto;
import org.generationcp.middleware.domain.workbench.RoleType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RemoveProgramMembersValidator {

	@Autowired
	private UserService userService;

	@Autowired
	private SecurityService securityService;

	public void validate(final String programUUID, final Set<Integer> userIds) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		//request is not null
		BaseValidator.checkNotNull(userIds, "param.null", new String[] {"userIds"});

		//at least one user is specified
		BaseValidator.checkNotEmpty(userIds, "program.member.user.not.specified");

		//check that any userId is null
		if (userIds.stream().anyMatch(Objects::isNull)) {
			errors.reject("program.member.null.user.id", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		//check that all userIds exists
		final List<Integer> existingUserIds =
			userService.getUsersByIds(new ArrayList<>(userIds)).stream().map(WorkbenchUser::getUserid)
				.collect(Collectors.toList());
		if (existingUserIds.size() != userIds.size()) {
			final List<Integer> invalidUsers = new ArrayList<>(userIds);
			invalidUsers.removeAll(existingUserIds);
			errors.reject("program.member.user.invalid", new String[] {Util.buildErrorMessageFromList(invalidUsers, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<Integer> usersWithProgramAccess =
			this.userService.getProgramMembers(programUUID, null, null).stream()
				.filter(p -> p.getRole().getType().equals(
					RoleType.PROGRAM.name())).map(ProgramMemberDto::getUserId).collect(Collectors.toList());
		if (!usersWithProgramAccess.containsAll(userIds)) {
			final List<Integer> notRemovableUserIds = new ArrayList<>(userIds);
			notRemovableUserIds.removeAll(usersWithProgramAccess);
			errors
				.reject("program.member.not.removable.user.ids", new String[] {Util.buildErrorMessageFromList(notRemovableUserIds, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		if (userIds.contains(loggedInUser.getUserid())) {
			throw new ApiRequestValidationException("program.member.self.not.removable", null);
		}

	}

}
