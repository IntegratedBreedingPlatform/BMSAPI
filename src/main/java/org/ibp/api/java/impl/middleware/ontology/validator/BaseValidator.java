package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;

import com.google.common.base.Strings;

/**
 * Helper methods to manage message codes.
 */
public abstract class BaseValidator {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected boolean isNonNullValidNumericString(Object value) {
		return value != null
				&& (value instanceof Integer || value instanceof String
						&& ((String) value).matches("^[0-9]+$"));
	}

	protected Integer getIntegerValueSafe(Object value, Integer defaultValue) {
		if (value instanceof Integer) {
			return (Integer) value;
		}

		if (value instanceof String) {
			return Integer.valueOf((String) value);
		}

		return defaultValue;
	}

	/**
	 * This function is useful to checking object value as null or empty with
	 * any plain object or from collection
	 * 
	 * @param value
	 *            value of object
	 * @return boolean
	 */
	@SuppressWarnings("rawtypes")
	protected boolean isNullOrEmpty(Object value) {
		return value instanceof String && Strings.isNullOrEmpty((String) value) || value == null
				|| value instanceof Collection && ((Collection) value).isEmpty()
				|| value instanceof Map && ((Map) value).isEmpty();
	}

	/**
	 * Adds the default error message into the current errors collection
	 *
	 * @param errors
	 *            The current errors collection
	 */
	protected void addDefaultError(Errors errors) {
		errors.reject("error.standard.defaultMessage");
	}

	/**
	 * Adds a "required" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addRequiredError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.required",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds a "required if" error for the given field name and field codes
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode1
	 *            The first field code
	 * @param fieldCode2
	 *            The second field code
	 */
	protected void addRequiredIfError(Errors errors, String fieldName, String fieldCode1,
			String fieldCode2) {
		Object[] arguments = BaseValidator.getCodesAsArguments(new String[] { fieldCode1,
				fieldCode2 });

		errors.rejectValue(fieldName, "error.standard.requiredIf", arguments, null);
	}

	/**
	 * Adds a "valid when" error for the given field name and field codes
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode1
	 *            The first field code
	 * @param fieldCode2
	 *            The second field code
	 */
	protected void addValidWhenError(Errors errors, String fieldName, String fieldCode1,
			String fieldCode2) {
		Object[] arguments = BaseValidator.getCodesAsArguments(new String[] { fieldCode1,
				fieldCode2 });

		errors.rejectValue(fieldName, "error.standard.validWhen", arguments, null);
	}

	/**
	 * Adds a "min length" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 * @param minLength
	 *            The minimum length of the field
	 */
	protected void addMinLengthError(Errors errors, String fieldName, String fieldCode,
			Integer minLength) {
		Object[] arguments = BaseValidator.getCodeAsArgument(fieldCode);
		arguments = BaseValidator.addArgument(arguments, minLength);

		errors.rejectValue(fieldName, "error.standard.minLength", arguments, null);
	}

	/**
	 * Adds a "max length" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 * @param maxLength
	 *            The maximum length of the field
	 */
	protected void addMaxLengthError(Errors errors, String fieldName, String fieldCode,
			Integer maxLength) {
		Object[] arguments = BaseValidator.getCodeAsArgument(fieldCode);
		arguments = BaseValidator.addArgument(arguments, maxLength);

		errors.rejectValue(fieldName, "error.standard.maxLength", arguments, null);
	}

	/**
	 * Adds a "min value" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 * @param minValue
	 *            The minimum value of the field
	 */
	protected void addMinValueError(Errors errors, String fieldName, String fieldCode, Integer minValue) {
		Object[] arguments = BaseValidator.getCodeAsArgument(fieldCode);
		arguments = BaseValidator.addArgument(arguments, minValue);

		errors.rejectValue(fieldName, "error.standard.minValue", arguments, null);
	}

	/**
	 * Adds a "max value" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 * @param maxValue
	 *            The maximum value of the field
	 */
	protected void addMaxValueError(Errors errors, String fieldName, String fieldCode, Integer maxValue) {
		Object[] arguments = BaseValidator.getCodeAsArgument(fieldCode);
		arguments = BaseValidator.addArgument(arguments, maxValue);

		errors.rejectValue(fieldName, "error.standard.maxValue", arguments, null);
	}

	/**
	 * Adds a "byte" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addByteError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.byte",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds a "short" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addShortError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.short",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds an "integer" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addIntegerError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.integer",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds a "long" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addLongError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.long",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds a "float" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addFloatError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.float",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds a "double" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addDoubleError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.double",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds a "date" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addDateError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.date",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds an "integer range" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 * @param min
	 *            The minimum value in the range
	 * @param max
	 *            The maximum value in the range
	 */
	protected void addIntegerRangeError(Errors errors, String fieldName, String fieldCode, Integer min,
			Integer max) {
		Object[] arguments = BaseValidator.getCodeAsArgument(fieldCode);
		arguments = BaseValidator.addArgument(arguments, min);
		arguments = BaseValidator.addArgument(arguments, max);

		errors.rejectValue(fieldName, "error.standard.range", arguments, null);
	}

	/**
	 * Adds a "float range" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 * @param min
	 *            The minimum value in the range
	 * @param max
	 *            The maximum value in the range
	 */
	protected void addFloatRangeError(Errors errors, String fieldName, String fieldCode, Float min,
			Float max) {
		Object[] arguments = BaseValidator.getCodeAsArgument(fieldCode);
		arguments = BaseValidator.addArgument(arguments, min);
		arguments = BaseValidator.addArgument(arguments, max);

		errors.rejectValue(fieldName, "error.standard.range", arguments, null);
	}

	/**
	 * Adds a "double range" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 * @param min
	 *            The minimum value in the range
	 * @param max
	 *            The maximum value in the range
	 */
	protected void addDoubleRangeError(Errors errors, String fieldName, String fieldCode,
			Double min, Double max) {
		Object[] arguments = BaseValidator.getCodeAsArgument(fieldCode);
		arguments = BaseValidator.addArgument(arguments, min);
		arguments = BaseValidator.addArgument(arguments, max);

		errors.rejectValue(fieldName, "error.standard.range", arguments, null);
	}

	/**
	 * Adds a "credit card" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addCreditCardError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.creditCard",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds an "e-mail" error for the given field name and field code
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param fieldCode
	 *            The given field code
	 */
	protected void addEmailError(Errors errors, String fieldName, String fieldCode) {
		errors.rejectValue(fieldName, "error.standard.email",
				BaseValidator.getCodeAsArgument(fieldCode), null);
	}

	/**
	 * Adds an custom error for the given field name, error code and arguments
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param errorCode
	 *            The given error code
	 * @param arguments
	 *            The given arguments
	 */
	protected void addCustomError(Errors errors, String fieldName, String errorCode,
			Object[] arguments) {
		errors.rejectValue(fieldName, errorCode, arguments, null);
	}

	/**
	 * Adds an custom error for the given field name, error code and resolvable
	 * arguments
	 *
	 * @param errors
	 *            The current errors collection
	 * @param fieldName
	 *            The given field name
	 * @param errorCode
	 *            The given error code
	 * @param resolvableArguments
	 *            The given resolvable arguments
	 */
	protected void addCustomErrorWithResolvableArguments(Errors errors, String fieldName,
			String errorCode, String[] resolvableArguments) {
		errors.rejectValue(fieldName, errorCode,
				BaseValidator.getCodesAsArguments(resolvableArguments), null);
	}

	/**
	 * Adds an custom error for the given error code and arguments
	 *
	 * @param errors
	 *            The current errors collection
	 * @param errorCode
	 *            The given error code
	 * @param arguments
	 *            The given arguments
	 */
	protected void addCustomError(Errors errors, String errorCode, Object[] arguments) {
		errors.reject(errorCode, arguments, null);
	}

	/**
	 * Adds an custom error for the given error code and resolvable arguments
	 *
	 * @param errors
	 *            The current errors collection
	 * @param errorCode
	 *            The given error code
	 * @param resolvableArguments
	 *            The given resolvable arguments
	 */
	protected void addCustomErrorWithResolvableArguments(Errors errors, String errorCode,
			String[] resolvableArguments) {
		errors.reject(errorCode, BaseValidator.getCodesAsArguments(resolvableArguments), null);
	}

	/**
	 * Adds a new argument to an existing argument array
	 *
	 * @param arguments
	 *            The existing argument array
	 * @param argument
	 *            The new argument to add to the array
	 * @return The updated array
	 */
	private static Object[] addArgument(Object[] arguments, Object argument) {
		List<Object> argumentList = new ArrayList<>();
		Collections.addAll(argumentList, arguments);
		argumentList.add(argument);
		return argumentList.toArray();
	}

	/**
	 * Converts a message code into a resolvable argument array
	 *
	 * @param messageCode
	 *            The message code
	 * @return The resolvable argument array
	 */
	private static Object[] getCodeAsArgument(String messageCode) {
		return new Object[] { new DefaultMessageSourceResolvable(new String[] { messageCode }) };
	}

	/**
	 * Converts an array of message codes into a resolvable argument array
	 *
	 * @param messageCodes
	 *            The array of message codes
	 * @return The resolvable argument array
	 */
	private static Object[] getCodesAsArguments(String[] messageCodes) {
		Object[] arguments = new Object[messageCodes.length];

		for (int i = 0; i < messageCodes.length; i++) {
			arguments[i] = new DefaultMessageSourceResolvable(new String[] { messageCodes[i] });
		}
		return arguments;
	}

}
