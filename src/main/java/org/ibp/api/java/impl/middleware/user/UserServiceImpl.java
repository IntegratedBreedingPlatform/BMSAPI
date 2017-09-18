package org.ibp.api.java.impl.middleware.user;

import com.google.common.base.Preconditions;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.domain.user.UserMapper;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.user.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	private static final String USER_NAME = "User";
	private static final String ERROR = "ERROR";

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	protected UserValidator userValidator;

	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public List<UserDetailDto> getAllUsersSortedByLastName() {
		final List<UserDetailDto> result = new ArrayList<>();
		final ModelMapper mapper = UserMapper.getInstance();
		final List<UserDto> users = this.workbenchDataManager.getAllUsersSortedByLastName();

		for (final UserDto userDto : users) {
			final UserDetailDto userInfo = mapper.map(userDto, UserDetailDto.class);
			result.add(userInfo);
		}
		return result;
	}

	@Override
	public Map<String, Object> createUser(final UserDetailDto user) {
		LOG.debug(user.toString());
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), UserServiceImpl.USER_NAME);
		final HashMap<String, Object> mapResponse = new HashMap<String, Object>();
		mapResponse.put("id", String.valueOf(0));

		userValidator.validate(user, errors, true);
		if (errors.hasErrors()) {
			LOG.debug("UserValidator returns errors");
			translateErrorToMap(errors, mapResponse);

		} else {

			final UserDto userdto = translateUserDetailsDtoToUserDto(user);
			userdto.setPassword(passwordEncoder.encode(userdto.getUsername()));

			try {
				final Integer newUserId = this.workbenchDataManager.createUser(userdto);
				mapResponse.put("id", String.valueOf(newUserId));

			} catch (MiddlewareQueryException e) {
				LOG.info("Error on workbenchDataManager.createUser ", e);
				errors.rejectValue(UserValidator.USER_ID, UserValidator.DATABASE_ERROR);
				translateErrorToMap(errors, mapResponse);
			}
		}

		return mapResponse;
	}

	@Override
	public Map<String, Object> updateUser(final UserDetailDto user) {
		LOG.debug(user.toString());
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), UserServiceImpl.USER_NAME);
		final HashMap<String, Object> mapResponse = new HashMap<String, Object>();
		mapResponse.put("id", String.valueOf(0));

		userValidator.validate(user, errors, false);
		if (errors.hasErrors()) {
			LOG.debug("UserValidator returns errors");
			translateErrorToMap(errors, mapResponse);

		} else {

			final UserDto userdto = translateUserDetailsDtoToUserDto(user);

			try {
				final Integer updateUserId = this.workbenchDataManager.updateUser(userdto);
				mapResponse.put("id", String.valueOf(updateUserId));
			} catch (MiddlewareQueryException e) {
				LOG.info("Error on workbenchDataManager.updateUser", e);
				errors.rejectValue(UserValidator.USER_ID, UserValidator.DATABASE_ERROR);
				translateErrorToMap(errors, mapResponse);
			}
		}

		return mapResponse;
	}

	@Override
	public List<UserDetailDto> getUsersByProjectUUID(final String projectUUID) {
		final List<UserDetailDto> result = new ArrayList<>();
		final ModelMapper mapper = UserMapper.getInstance();

		Preconditions.checkNotNull(projectUUID, "The projectUUID must not be empty");

		try {
			List<UserDto> users = this.workbenchDataManager.getUsersByProjectUuid(projectUUID);
			Preconditions.checkArgument(!users.isEmpty(), "users don't exists for this projectUUID");

			for (final UserDto userDto : users) {
				final UserDetailDto userInfo = mapper.map(userDto, UserDetailDto.class);
				result.add(userInfo);
			}

		} catch (MiddlewareQueryException e) {
			LOG.info("Error on workbenchDataManager.getUsersByProjectUuid", e);
			throw new ApiRuntimeException("An internal error occurred while trying to get the users");
		}
		return result;
	}

	private UserDto translateUserDetailsDtoToUserDto(final UserDetailDto user) {
		final UserDto userdto = new UserDto();
		userdto.setUserId(user.getId());
		userdto.setUsername(user.getUsername());
		userdto.setFirstName(user.getFirstName());
		userdto.setLastName(user.getLastName());
		userdto.setRole(user.getRole());
		userdto.setEmail(user.getEmail());
		userdto.setStatus("true".equals(user.getStatus()) ? 0 : 1);
		return userdto;
	}

	public void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setUserValidator(final UserValidator userValidator) {
		this.userValidator = userValidator;
	}

	public void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	private void translateErrorToMap(final BindingResult errors, final HashMap<String, Object> mapErrors) {
		ErrorResponse errResponse = new ErrorResponse();

		if (errors.getFieldErrorCount(UserValidator.FIRST_NAME) != 0) {
			final String errorName = errors.getFieldError(UserValidator.FIRST_NAME).getCode();
			errResponse.addError(translateCodeErrorValidator(errorName), UserValidator.FIRST_NAME);
		}

		if (errors.getFieldErrorCount(UserValidator.LAST_NAME) != 0) {
			final String errorLastName = errors.getFieldError(UserValidator.LAST_NAME).getCode();
			errResponse.addError(translateCodeErrorValidator(errorLastName), UserValidator.LAST_NAME);
		}

		if (errors.getFieldErrorCount(UserValidator.USERNAME) != 0) {
			final String errorUserName = errors.getFieldError(UserValidator.USERNAME).getCode();
			errResponse.addError(translateCodeErrorValidator(errorUserName), UserValidator.USERNAME);
		}

		if (errors.getFieldErrorCount(UserValidator.EMAIL) != 0) {
			final String errorEmail = errors.getFieldError(UserValidator.EMAIL).getCode();
			errResponse.addError(translateCodeErrorValidator(errorEmail), UserValidator.EMAIL);
		}

		if (errors.getFieldErrorCount(UserValidator.ROLE) != 0) {
			final String errorRole = errors.getFieldError(UserValidator.ROLE).getCode();
			errResponse.addError(translateCodeErrorValidator(errorRole), UserValidator.ROLE);
		}

		if (errors.getFieldErrorCount(UserValidator.STATUS) != 0) {
			final String errorStatus = errors.getFieldError(UserValidator.STATUS).getCode();
			errResponse.addError(translateCodeErrorValidator(errorStatus), UserValidator.STATUS);
		}

		if (errors.getFieldErrorCount(UserValidator.USER_ID) != 0) {
			final String errorUserId = errors.getFieldError(UserValidator.USER_ID).getCode();
			errResponse.addError(translateCodeErrorValidator(errorUserId), UserValidator.USER_ID);
		}

		if (errors.getGlobalErrorCount() != 0) {
			List<ObjectError> globalErrors = errors.getGlobalErrors();
			for (ObjectError globalError : globalErrors) {
				errResponse.addError(globalError.getCode());
			}
		}

		mapErrors.put(ERROR, errResponse);
	}

	private String translateCodeErrorValidator(final String codeError) {

		if (UserValidator.SIGNUP_FIELD_INVALID_EMAIL_FORMAT.equals(codeError)) {
			return "invalid format";
		}
		if (UserValidator.SIGNUP_FIELD_REQUIRED.equals(codeError)) {
			return "field required";
		}
		if (UserValidator.SIGNUP_FIELD_LENGTH_EXCEED.equals(codeError)) {
			return "lentgh exceed";
		}
		if (UserValidator.SIGNUP_FIELD_EMAIL_EXISTS.equals(codeError)) {
			return "exists";
		}
		if (UserValidator.SIGNUP_FIELD_USERNAME_EXISTS.equals(codeError)) {
			return "exists";
		}
		if (UserValidator.SIGNUP_FIELD_INVALID_ROLE.equals(codeError)) {
			return "invalid";
		}
		if (UserValidator.DATABASE_ERROR.equals(codeError)) {
			return "DB error";
		}
		if (UserValidator.SIGNUP_FIELD_INVALID_USER_ID.equals(codeError)) {
			return "invalid";
		}
		return "";
	}

}
