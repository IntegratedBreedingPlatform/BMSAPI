package org.ibp.api.java.impl.middleware.user;

import com.google.common.base.Preconditions;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.domain.user.UserMapper;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.user.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
	private WorkbenchDataManager workbenchDataManager;

	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public List<UserDetailDto> getAllUsersSortedByLastName() {
		final List<UserDetailDto> result = new ArrayList<>();
		final ModelMapper mapper = UserMapper.getInstance();
		final List<UserDto> users = this.userService.getAllUsersSortedByLastName();

		for (final UserDto user : users) {
			final UserDetailDto userDetailDto = mapper.map(user, UserDetailDto.class);
			result.add(userDetailDto);
		}
		return result;
	}

	@Override
	public Integer createUser(final UserDetailDto user) {
		this.userValidator.validate(user, true);

		final UserDto userdto = this.translateUserDetailsDtoToUserDto(user);
		userdto.setPassword(this.passwordEncoder.encode(userdto.getUsername()));

		return this.userService.createUser(userdto);
	}

	@Override
	public Integer updateUser(final UserDetailDto user) {
		this.userValidator.validate(user, false);

		final UserDto userdto = this.translateUserDetailsDtoToUserDto(user);
		return this.userService.updateUser(userdto);
	}

	@Override
	public List<UserDetailDto> getUsersByProjectUUID(final String projectUUID) {
		final List<UserDetailDto> result = new ArrayList<>();
		final ModelMapper mapper = UserMapper.getInstance();

		Preconditions.checkNotNull(projectUUID, "The projectUUID must not be empty");
		try {
			final Project project = this.workbenchDataManager.getProjectByUuid(projectUUID);
			if (project != null) {
				//TODO Create a new mapper to map from WorkbenchUser to UserDto, out of the scope for IBP-2792
				final List<WorkbenchUser> workbenchUsers = this.userService.getUsersByProjectId(project.getProjectId());
				Preconditions.checkArgument(!workbenchUsers.isEmpty(), "users don't exists for this projectUUID");

				final List<UserDto> users = workbenchUsers.stream().map(wu -> new UserDto(wu)).collect(Collectors.toList());

				for (final UserDto userDto : users) {
					final UserDetailDto userInfo = mapper.map(userDto, UserDetailDto.class);
					result.add(userInfo);
				}
			}
		} catch (final MiddlewareQueryException e) {
			LOG.info("Error on userService.getUsersByProjectUuid", e);
			throw new ApiRuntimeException("An internal error occurred while trying to get the users");
		}
		return result;
	}

	@Override
	@Transactional
	public UserDto getUserWithAuthorities(final String cropName, final String programUuid) {
		final String userName = SecurityUtil.getLoggedInUserName();
		final WorkbenchUser user = this.userService.getUserWithAuthorities(userName, cropName, programUuid);
		final ModelMapper userMapper = UserMapper.getInstance();
		return userMapper.map(user, UserDto.class);
	}

	private UserDto translateUserDetailsDtoToUserDto(final UserDetailDto user) {
		final UserDto userdto = new UserDto();
		userdto.setUserId(user.getId());
		userdto.setUsername(user.getUsername());
		userdto.setFirstName(user.getFirstName());
		userdto.setLastName(user.getLastName());
		userdto.setUserRoles(user.getUserRoles());
		userdto.setEmail(user.getEmail());
		userdto.setStatus("true".equals(user.getStatus()) ? 0 : 1);
		userdto.setCrops(user.getCrops());

		if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
			final String userName = SecurityUtil.getLoggedInUserName();
			final WorkbenchUser workbenchUser = this.userService.getUserByUsername(userName);
			userdto.getUserRoles().forEach(userRoleDto -> {
				if (user.getId() == null || user.getId() == 0 || userRoleDto.getCreatedBy() == null || userRoleDto.getCreatedBy() == 0) {
					userRoleDto.setCreatedBy(workbenchUser.getUserid());
				}
			});
		}

		return userdto;
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
