package org.ibp.api.java.impl.middleware.user;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.dao.workbench.ProgramEligibleUsersSearchRequest;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.user.UserMapper;
import org.ibp.api.domain.user.UserProfileUpdateRequestDTO;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.user.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private org.generationcp.middleware.service.api.user.UserService userService;

	@Autowired
	protected UserValidator userValidator;

	@Autowired
	protected SecurityService securityService;

	@Autowired
	private ProgramService programService;

	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public List<UserDto> getAllUsersSortedByLastName() {
		return this.userService.getAllUsersSortedByLastName();
	}

	@Override
	public Integer createUser(final UserDto user) {
		this.userValidator.validate(user, true);
		user.setPassword(this.passwordEncoder.encode(user.getUsername()));
		return this.userService.createUser(user);
	}

	@Override
	public Integer updateUser(final UserDto user) {
		this.userValidator.validate(user, false);
		return this.userService.updateUser(user);
	}

	@Override
	public List<UserDto> getUsersByProjectUUID(final String projectUUID) {
		Preconditions.checkNotNull(projectUUID, "The projectUUID must not be empty");
		try {
			final Project project = this.programService.getProjectByUuid(projectUUID);
			if (project != null) {
				//TODO Create a new mapper to map from WorkbenchUser to UserDto, out of the scope for IBP-2792
				final List<WorkbenchUser> workbenchUsers = this.userService.getUsersByProjectId(project.getProjectId());
				Preconditions.checkArgument(!workbenchUsers.isEmpty(), "users don't exists for this projectUUID");

				final List<UserDto> users = workbenchUsers.stream().map(wu -> new UserDto(wu)).collect(Collectors.toList());
				return users;
			}
		} catch (final MiddlewareQueryException e) {
			LOG.info("Error on userService.getUsersByProjectUuid", e);
			throw new ApiRuntimeException("An internal error occurred while trying to get the users");
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	@Transactional
	public UserDto getUserWithAuthorities(final String cropName, final String programUuid) {
		final String userName = SecurityUtil.getLoggedInUserName();
		final WorkbenchUser user = this.userService.getUserWithAuthorities(userName, cropName, programUuid);
		final ModelMapper userMapper = UserMapper.getInstance();
		final UserDto userDto = userMapper.map(user, UserDto.class);
		return userDto;
	}

	@Override
	public void updateUserProfile(final UserProfileUpdateRequestDTO userProfileUpdateRequestDTO, final WorkbenchUser workbenchUser) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), UserProfileUpdateRequestDTO.class.getName());
		final UserDto userDto = new UserDto(workbenchUser);

		if (!StringUtils.isBlank(userProfileUpdateRequestDTO.getFirstName()) && !userDto.getFirstName()
			.equals(userProfileUpdateRequestDTO.getFirstName())) {
			this.userValidator
				.validateFieldLength(errors, userProfileUpdateRequestDTO.getFirstName(), UserValidator.FIRST_NAME,
					UserValidator.FIRST_NAME_MAX_LENGTH);
			userDto.setFirstName(userProfileUpdateRequestDTO.getFirstName());
		}

		if (!StringUtils.isBlank(userProfileUpdateRequestDTO.getLastName()) && !userDto.getLastName()
			.equals(userProfileUpdateRequestDTO.getLastName())) {
			this.userValidator
				.validateFieldLength(errors, userProfileUpdateRequestDTO.getLastName(), UserValidator.LAST_NAME,
					UserValidator.LAST_NAME_MAX_LENGTH);
			userDto.setLastName(userProfileUpdateRequestDTO.getLastName());
		}

		if (!StringUtils.isBlank(userProfileUpdateRequestDTO.getEmail()) && !userProfileUpdateRequestDTO
			.getEmail().equals(workbenchUser.getPerson().getEmail())) {
			this.userValidator.validateEmail(errors, userProfileUpdateRequestDTO.getEmail());
			userDto.setEmail(userProfileUpdateRequestDTO.getEmail());
		}

		this.userService.updateUser(userDto);
	}

	@Override
	public List<UserDto> getMembersEligibleUsers(final String programUUID, final ProgramEligibleUsersSearchRequest searchRequest,
		final Pageable pageable) {
		return this.userService.getProgramMembersEligibleUsers(programUUID, searchRequest, pageable);
	}

	@Override
	public long countAllMembersEligibleUsers(final String programUUID, final ProgramEligibleUsersSearchRequest searchRequest) {
		return this.userService.countProgramMembersEligibleUsers(programUUID, searchRequest);
	}

	public void setSecurityService(final SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setUserValidator(final UserValidator userValidator) {
		this.userValidator = userValidator;
	}

	public void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setUserService(final org.generationcp.middleware.service.api.user.UserService userService) {
		this.userService = userService;
	}

}
