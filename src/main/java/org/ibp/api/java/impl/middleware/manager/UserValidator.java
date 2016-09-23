
package org.ibp.api.java.impl.middleware.manager;

import java.util.regex.Pattern;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.ibp.api.brapi.v1.user.UserDetailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

	private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

	private static final String SIGNUP_FIELD_INVALID_EMAIL_FORMAT = "signup.field.email.invalid";
	private static final String SIGNUP_FIELD_REQUIRED = "signup.field.required";
	private static final String SIGNUP_FIELD_LENGTH_EXCEED = "signup.field.length.exceed";
	private static final String SIGNUP_FIELD_EMAIL_EXISTS = "signup.field.email.exists";
	private static final String SIGNUP_FIELD_USERNAME_EXISTS = "signup.field.username.exists";
	private static final String SIGNUP_FIELD_INVALID_ROLE = "signup.field.invalid.role";
	private static final String SIGNUP_FIELD_INVALID_STATUS = "signup.field.invalid.status";
	private static final String SIGNUP_FIELD_INVALID_USER_ID = "signup.field.invalid.userId";

	private static final String DATABASE_ERROR = "database.error";

	private static final String FIRST_NAME_STR = "First Name";
	private static final String LAST_NAME_STR = "Last Name";
	private static final String USERNAME_STR = "Username";
	private static final String EMAIL_STR = "Email";
	private static final String ROLE_STR = "Role";
	private static final String STATUS_STR = "Status";

	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String EMAIL = "email";
	private static final String USERNAME = "username";
	private static final String ROLE = "role";
	private static final String STATUS = "status";
	private static final String USER_ID = "userId";

	private static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	@Autowired
	protected WorkbenchDataManager workbenchDataManager;

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	@Override
	public boolean supports(Class<?> aClass) {
		return UserDetailDto.class.equals(aClass);
	}

	@Override
	public void validate(Object o, Errors errors) {
		UserDetailDto user = (UserDetailDto) o;

		this.validateFieldLength(errors, user.getFirstName(), FIRST_NAME, FIRST_NAME_STR, 20);
		this.validateFieldLength(errors, user.getLastName(), LAST_NAME, LAST_NAME_STR, 50);
		this.validateFieldLength(errors, user.getUsername(), USERNAME, USERNAME_STR, 30);
		this.validateFieldLength(errors, user.getEmail(), EMAIL, EMAIL_STR, 40);
		this.validateFieldLength(errors, user.getRole(), ROLE, ROLE_STR, 30);
		this.validateFieldLength(errors, user.getStatus(), STATUS, STATUS_STR, 11);

		this.validateUserRole(errors, user.getRole());

		this.validateEmailFormat(errors, user.getEmail());

		this.validateUserId(errors, user.getId());

		this.validateUserStatus(errors, user.getStatus());

		if (validateUserCreate(user)) {
			this.validateUsernameIfExists(errors, user.getUsername());

			this.validatePersonEmailIfExists(errors, user.getEmail());
		} else {
			this.validateUserUpdate(errors, user);
		}

	}

	private boolean validateUserCreate(final UserDetailDto user) {
		return user.getId() == null || 0 == user.getId();
	}

	private void validateUserUpdate(Errors errors, UserDetailDto user) {
		User userUpdate = this.workbenchDataManager.getUserById(user.getId());

		if (userUpdate != null) {
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

	private void validateUserId(final Errors errors, final Integer userId) {
		if (null == userId) {
			errors.rejectValue(USER_ID, SIGNUP_FIELD_REQUIRED);
		}

	}

	protected void validateEmailFormat(final Errors errors, final String eMail) {
		if (null != eMail && !Pattern.compile(EMAIL_PATTERN).matcher(eMail).matches()) {
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
			if (this.workbenchDataManager.isUsernameExists(userName)) {
				errors.rejectValue(USERNAME, SIGNUP_FIELD_USERNAME_EXISTS, new String[] {userName}, null);
			}
		} catch (MiddlewareQueryException e) {
			errors.rejectValue(USERNAME, DATABASE_ERROR);
			LOG.error(e.getMessage(), e);
		}
	}

	protected void validatePersonEmailIfExists(final Errors errors, final String eMail) {
		try {
			if (this.workbenchDataManager.isPersonWithEmailExists(eMail)) {
				errors.rejectValue(EMAIL, SIGNUP_FIELD_EMAIL_EXISTS);
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	protected void validateUserRole(final Errors errors, final String fieldvalue) {
		if (fieldvalue != null && !fieldvalue.equalsIgnoreCase("ADMIN") && !fieldvalue.equalsIgnoreCase("BREEDER")
				&& !fieldvalue.equalsIgnoreCase("TECHNICIAN")) {
			errors.rejectValue(ROLE, SIGNUP_FIELD_INVALID_ROLE);
		}
	}

	protected void validateUserStatus(final Errors errors, final String fieldvalue) {
		if (fieldvalue != null && !fieldvalue.equalsIgnoreCase("true") && !fieldvalue.equalsIgnoreCase("false")) {
			errors.rejectValue(STATUS, SIGNUP_FIELD_INVALID_STATUS);
		}
	}
}
