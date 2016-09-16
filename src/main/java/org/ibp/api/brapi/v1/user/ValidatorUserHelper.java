
package org.ibp.api.brapi.v1.user;

import java.util.List;
import java.util.regex.Pattern;

import org.generationcp.middleware.service.api.user.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ValidatorUserHelper {

	private static final Logger LOG = LoggerFactory.getLogger(ValidatorUserHelper.class);

	protected static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public static final String SIGNUP_FIELD_REQUIRED = "signup.field.required";
	public static final String SIGNUP_FIELD_INVALID_ROLE = "signup.field.invalid.role";
	public static final String SIGNUP_FIELD_LENGTH_EXCEED = "signup.field.length.exceed";
	public static final String SIGNUP_FIELD_INVALID_EMAIL_FORMAT = "signup.field.email.invalid";

	public static final String FIRST_NAME_STR = "First Name";
	public static final String LAST_NAME_STR = "Last Name";
	public static final String USERNAME_STR = "Username";
	public static final String EMAIL_STR = "Email";
	public static final String ROLE_STR = "Role";

	public static boolean validate(UserDto userdto, List<String> errors) {
		validateFieldLength(errors, userdto.getFirstName(), FIRST_NAME_STR, 20);
		validateFieldLength(errors, userdto.getLastName(), LAST_NAME_STR, 50);
		validateFieldLength(errors, userdto.getEmail(), EMAIL_STR, 40);
		validateFieldLength(errors, userdto.getUsername(), USERNAME_STR, 30);
		validateFieldLength(errors, userdto.getRole(), ROLE_STR, 30);

		validateEmailFormat(errors, userdto.getEmail());

		validateUserRole(errors, userdto.getRole());

		return !errors.isEmpty() ? false : true;
	}

	private static void validateFieldLength(List<String> errors, String fieldvalue, String fieldname, Integer maxLength) {
		if (maxLength < fieldvalue.length()) {
			errors.add(fieldname + " = " + SIGNUP_FIELD_LENGTH_EXCEED);
			LOG.info(fieldname + " = " + SIGNUP_FIELD_LENGTH_EXCEED);
		}
		if (0 == fieldvalue.length()) {
			errors.add(fieldname + " = " + SIGNUP_FIELD_REQUIRED);
			LOG.info(fieldname + " = " + SIGNUP_FIELD_REQUIRED);
		}

	}

	private static void validateEmailFormat(List<String> errors, String fieldvalue) {
		if (!Pattern.compile(EMAIL_PATTERN).matcher(fieldvalue).matches()) {
			errors.add(EMAIL_STR + " = " + SIGNUP_FIELD_INVALID_EMAIL_FORMAT);
			LOG.info(EMAIL_STR + " = " + SIGNUP_FIELD_INVALID_EMAIL_FORMAT);
		}
	}

	private static void validateUserRole(List<String> errors, String fieldvalue) {
		if (!fieldvalue.equalsIgnoreCase("ADMIN") && !fieldvalue.equalsIgnoreCase("BREEDER") && !fieldvalue.equalsIgnoreCase("TECHNICIAN")) {
			errors.add(ROLE_STR + " = " + SIGNUP_FIELD_INVALID_ROLE);
			LOG.info(ROLE_STR + " = " + SIGNUP_FIELD_INVALID_ROLE);
		}
	}

}
