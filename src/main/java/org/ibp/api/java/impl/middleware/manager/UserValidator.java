
package org.ibp.api.java.impl.middleware.manager;

import java.util.regex.Pattern;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
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

	private static final String DATABASE_ERROR = "database.error";

	private static final String FIRST_NAME_STR = "First Name";
	private static final String LAST_NAME_STR = "Last Name";
	private static final String USERNAME_STR = "Username";
	private static final String EMAIL_STR = "Email";
	private static final String ROLE_STR = "Role";

	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String EMAIL = "email";
	private static final String USERNAME = "username";
	private static final String ROLE = "role";

	@Autowired
	protected WorkbenchDataManager workbenchDataManager;

	private static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	@Override
	public boolean supports(Class<?> aClass) {
		return UserDto.class.equals(aClass);
	}

	@Override
	public void validate(Object o, Errors errors) {
		UserDto userAccount = (UserDto) o;

		this.validateFieldLength(errors, userAccount.getFirstName(), FIRST_NAME, FIRST_NAME_STR, 20);
		this.validateFieldLength(errors, userAccount.getLastName(), LAST_NAME, LAST_NAME_STR, 50);
		this.validateFieldLength(errors, userAccount.getUsername(), USERNAME, USERNAME_STR, 30);
		this.validateFieldLength(errors, userAccount.getEmail(), EMAIL, EMAIL_STR, 40);
		this.validateFieldLength(errors, userAccount.getRole(), ROLE, ROLE_STR, 30);

		this.validateUserRole(errors, userAccount.getRole());

		this.validateEmailFormat(errors, userAccount);

		if (0 == userAccount.getUserId()) {
			this.validateUsernameIfExists(errors, userAccount);

			this.validatePersonEmailIfExists(errors, userAccount);
		}
	}

	protected void validateEmailFormat(Errors errors, UserDto userAccount) {
		if (!Pattern.compile(EMAIL_PATTERN).matcher(userAccount.getEmail()).matches()) {
			errors.rejectValue(EMAIL, SIGNUP_FIELD_INVALID_EMAIL_FORMAT);
		}
	}

	protected void validateFieldLength(Errors errors, String fieldValue, String fieldProperty, String fieldName, Integer maxLength) {

		if (maxLength < fieldValue.length()) {
			errors.rejectValue(fieldProperty, SIGNUP_FIELD_LENGTH_EXCEED, new String[] {Integer.toString(maxLength), fieldName}, null);
		}
		if (null == fieldValue || 0 == fieldValue.trim().length()) {
			errors.rejectValue(fieldProperty, SIGNUP_FIELD_REQUIRED, new String[] {Integer.toString(maxLength), fieldName}, null);
		}
	}

	protected void validateUsernameIfExists(Errors errors, UserDto userAccount) {
		try {
			if (this.workbenchDataManager.isUsernameExists(userAccount.getUsername())) {
				errors.rejectValue(USERNAME, SIGNUP_FIELD_USERNAME_EXISTS, new String[] {userAccount.getUsername()}, null);
			}
		} catch (MiddlewareQueryException e) {
			errors.rejectValue(USERNAME, DATABASE_ERROR);
			LOG.error(e.getMessage(), e);
		}
	}

	protected void validatePersonEmailIfExists(Errors errors, UserDto userAccount) {
		try {
			if (this.workbenchDataManager.isPersonWithEmailExists(userAccount.getEmail())) {
				errors.rejectValue(EMAIL, SIGNUP_FIELD_EMAIL_EXISTS);
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	protected void validateUserRole(Errors errors, String fieldvalue) {
		if (!fieldvalue.equalsIgnoreCase("ADMIN") && !fieldvalue.equalsIgnoreCase("BREEDER")
				&& !fieldvalue.equalsIgnoreCase("TECHNICIAN")) {
			errors.rejectValue(ROLE_STR, SIGNUP_FIELD_INVALID_ROLE, new String[] {ROLE_STR}, null);
		}
	}
}
