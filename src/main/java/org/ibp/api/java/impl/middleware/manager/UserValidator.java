
package org.ibp.api.java.impl.middleware.manager;

import java.util.regex.Pattern;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

	private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

	public static final String SIGNUP_FIELD_INVALID_EMAIL_FORMAT = "signup.field.email.invalid";
	public static final String SIGNUP_FIELD_REQUIRED = "signup.field.required";
	public static final String SIGNUP_FIELD_LENGTH_EXCEED = "signup.field.length.exceed";
	public static final String SIGNUP_FIELD_EMAIL_EXISTS = "signup.field.email.exists";
	public static final String SIGNUP_FIELD_USERNAME_EXISTS = "signup.field.username.exists";
	public static final String SIGNUP_FIELD_INVALID_ROLE = "signup.field.invalid.role";
	public static final String SIGNUP_FIELD_INVALID_STATUS = "signup.field.invalid.status";
	public static final String SIGNUP_FIELD_INVALID_USER_ID = "signup.field.invalid.userId";
	public static final String USER_AUTO_DEACTIVATION = "A user cannot be auto-deactivated";

	public static final String DATABASE_ERROR = "database.error";

	public static final String FIRST_NAME_STR = "First Name";
	public static final String LAST_NAME_STR = "Last Name";
	public static final String USERNAME_STR = "Username";
	public static final String EMAIL_STR = "Email";
	public static final String ROLE_STR = "Role";
	public static final String STATUS_STR = "Status";

	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String EMAIL = "email";
	public static final String USERNAME = "username";
	public static final String ROLE = "role";
	public static final String STATUS = "status";
	public static final String USER_ID = "userId";

	private static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	@Autowired
	protected WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SecurityService securityService;

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setSecurityService(final SecurityService securityService) {
		this.securityService = securityService;
	}

	@Override
	public boolean supports(Class<?> aClass) {
		return UserDetailDto.class.equals(aClass);
	}

	@Override
	public void validate(final Object o, final Errors errors) {

	}

	public void validate(final Object o, final Errors errors, final boolean createUser) {
		UserDetailDto user = (UserDetailDto) o;

		this.validateFieldLength(errors, user.getFirstName(), FIRST_NAME, FIRST_NAME_STR, 20);
		this.validateFieldLength(errors, user.getLastName(), LAST_NAME, LAST_NAME_STR, 50);
		this.validateFieldLength(errors, user.getUsername(), USERNAME, USERNAME_STR, 30);
		this.validateFieldLength(errors, user.getEmail(), EMAIL, EMAIL_STR, 40);
		this.validateFieldLength(errors, user.getStatus(), STATUS, STATUS_STR, 11);

		this.validateUserRole(errors, user.getRole());
		this.validateEmailFormat(errors, user.getEmail());

		this.validateUserStatus(errors, user.getStatus());

		if (createUser) {
			this.validateUsernameIfExists(errors, user.getUsername());

			this.validatePersonEmailIfExists(errors, user.getEmail());
		} else {
			this.validateUserId(errors, user.getId());

			this.validateUserUpdate(errors, user);
		}

	}

	private void validateUserUpdate(Errors errors, UserDetailDto user) {
		WorkbenchUser userUpdate = null;
		if (null == errors.getFieldError(USER_ID)) {
			try {
				userUpdate = this.workbenchDataManager.getUserById(user.getId());
			} catch (MiddlewareQueryException e) {
				errors.rejectValue(USER_ID, DATABASE_ERROR);
				LOG.error(e.getMessage(), e);
			}

			if (userUpdate != null) {
				WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
				// TODO change frontend status type to integer
				if (loggedInUser.equals(userUpdate) && "false".equals(user.getStatus())) {
					errors.reject(USER_AUTO_DEACTIVATION);
				}

				if (!userUpdate.getName().equalsIgnoreCase(user.getUsername())) {
					this.validateUsernameIfExists(errors, user.getUsername());
				}

				if (!userUpdate.getPerson().getEmail().equalsIgnoreCase(user.getEmail())) {
					this.validatePersonEmailIfExists(errors, user.getEmail());
				}
			} else {
				errors.rejectValue(USER_ID, SIGNUP_FIELD_INVALID_USER_ID);
			}
		}
	}

	private void validateUserId(final Errors errors, final Integer userId) {
		if (null == userId) {
			errors.rejectValue(USER_ID, SIGNUP_FIELD_REQUIRED);
		}

	}

	protected void validateEmailFormat(final Errors errors, final String eMail) {
		if (null == errors.getFieldError(EMAIL) && null != eMail && !Pattern.compile(EMAIL_PATTERN).matcher(eMail).matches()) {
			errors.rejectValue(EMAIL, SIGNUP_FIELD_INVALID_EMAIL_FORMAT);
		}
	}

	protected void validateFieldLength(final Errors errors, final String fieldValue, final String fieldProperty, final String fieldName,
			final Integer maxLength) {

		if (null == fieldValue || 0 == fieldValue.trim().length()) {
			errors.rejectValue(fieldProperty, SIGNUP_FIELD_REQUIRED, new String[] {Integer.toString(maxLength), fieldName}, null);
		}

		if (null != fieldValue && maxLength < fieldValue.length()) {
			errors.rejectValue(fieldProperty, SIGNUP_FIELD_LENGTH_EXCEED, new String[] {Integer.toString(maxLength), fieldName}, null);
		}

	}

	protected void validateUsernameIfExists(final Errors errors, final String userName) {
		try {
			if (null == errors.getFieldError(USERNAME) && this.workbenchDataManager.isUsernameExists(userName)) {
				errors.rejectValue(USERNAME, SIGNUP_FIELD_USERNAME_EXISTS, new String[] {userName}, null);
			}
		} catch (MiddlewareQueryException e) {
			errors.rejectValue(USERNAME, DATABASE_ERROR);
			LOG.error(e.getMessage(), e);
		}
	}

	protected void validatePersonEmailIfExists(final Errors errors, final String eMail) {
		try {
			if (null == errors.getFieldError(EMAIL) && this.workbenchDataManager.isPersonWithEmailExists(eMail)) {
				errors.rejectValue(EMAIL, SIGNUP_FIELD_EMAIL_EXISTS);
			}
		} catch (MiddlewareQueryException e) {
			errors.rejectValue(EMAIL, DATABASE_ERROR);
			LOG.error(e.getMessage(), e);
		}
	}

    protected void validateUserRole(final Errors errors, final Role role) {
		if (null == role) {
            errors.rejectValue(ROLE, SIGNUP_FIELD_INVALID_ROLE);
		} else {
	        this.validateFieldLength(errors, role.getDescription(), ROLE, ROLE_STR, 30);
		}

    }
    
	protected void validateUserStatus(final Errors errors, final String fieldValue) {
		if (null == errors.getFieldError(STATUS) && fieldValue != null && !"true".equalsIgnoreCase(fieldValue)
				&& !"false".equalsIgnoreCase(fieldValue)) {
			errors.rejectValue(STATUS, SIGNUP_FIELD_INVALID_STATUS);
		}
	}
}
