
package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.base.Strings;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Helper methods to manage message codes.
 */
public abstract class BaseValidator {

	protected static final String INVALID_ID = "id.is.invalid.format";
	protected static final String INVALID_TYPE_ID = "type.id.is.invalid.format";
	protected static final String ID_DOES_NOT_EXIST = "id.does.not.exist";
	protected static final String FIELD_IS_REQUIRED = "field.is.required";
	protected static final String FIELD_SHOULD_BE_NUMERIC = "field.should.be.numeric";
	protected static final String TEXTUAL_FIELD_IS_TOO_LONG = "textual.field.is.too.long";
	protected static final String LIST_TEXTUAL_FIELD_IS_TOO_LONG = "list.textual.field.is.too.long";
	protected static final String NAME_ALREADY_EXIST = "name.already.exist";
	protected static final String NAME_OR_ALIAS_ALREADY_EXIST = "name.or.alias.already.exist";
	protected static final String NAME_AND_ALIAS_ARE_EQUALS = "name.and.alias.equals";
	protected static final String RECORD_IS_NOT_EDITABLE = "record.is.not.editable";
	protected static final String RECORD_IS_NOT_DELETABLE = "record.is.not.deletable";
	protected static final String RECORD_RELATED_WITH_A_GERMPLASM_DELETED = "record.with.germplasm.deleted";
	protected static final String LIST_SHOULD_NOT_BE_EMPTY = "list.should.not.be.empty";
	protected static final String MIN_MAX_NOT_VALID = "min.max.not.valid";
	protected static final String MIN_SHOULD_NOT_GREATER_THEN_MAX = "min.should.not.be.greater.than.max";

	protected boolean isNonNullValidNumericString(Object value) {
		return value != null && (value instanceof Integer || value instanceof String && ((String) value).matches("^[0-9]+$"));
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

	protected void checkNumberField(String value, Errors errors) {
		if (value.matches("^[0-9]+$")) {
			try {
				Integer.valueOf(value);
				return;
			} catch (Exception ignored) {
				this.addCustomError(errors, BaseValidator.INVALID_ID, null);
				return;
			}
		}
		this.addCustomError(errors, BaseValidator.INVALID_ID, null);
	}

	public static void checkNotNull(final Object reference, final String errorCode, final Object[] arguments) {
		if (reference == null) {
			throw new ApiRequestValidationException(Collections.singletonList(new ObjectError("", new String[]{errorCode}, arguments, "")));
		}
	}

	public static void checkNotNull(final Object reference, final String errorCode) {
		checkNotNull(reference, errorCode, null);
	}

	public static void checkArgument(final boolean expression, final String errorCode) {
		checkArgument(expression, errorCode, null);
	}

	public static void checkArgument(final boolean expression, final String errorCode, final Object[] arguments) {
		if (!expression) {
			throw new ApiRequestValidationException(Collections.singletonList(new ObjectError("", new String[]{errorCode}, arguments, "")));
		}
	}

	public static void checkNotEmpty(final Object reference, final String errorCode) {
		checkNotEmpty(reference, errorCode, null);
	}

	public static void checkNotEmpty(final Object reference, final String errorCode, final Object[] arguments) {
		if (isNullOrEmpty(reference)) {
			throw new ApiRequestValidationException(Collections.singletonList(new ObjectError("", new String[]{errorCode}, arguments, "")));
		}
	}

	/**
	 * This function is useful to checking object value as null or empty with any plain object or from collection
	 * 
	 * @param value value of object
	 * @return boolean
	 */
	@SuppressWarnings("rawtypes")
	protected static boolean isNullOrEmpty(Object value) {
		return value instanceof String && Strings.isNullOrEmpty(((String) value).trim()) || value == null || value instanceof Collection
				&& ((Collection) value).isEmpty() || value instanceof Map && ((Map) value).isEmpty();
	}

	/**
	 * Adds an custom error for the given field name, error code and arguments
	 *
	 * @param errors The current errors collection
	 * @param fieldName The given field name
	 * @param errorCode The given error code
	 * @param arguments The given arguments
	 */
	protected void addCustomError(Errors errors, String fieldName, String errorCode, Object[] arguments) {
		errors.rejectValue(fieldName, errorCode, arguments, null);
	}

	/**
	 * Adds an custom error for the given error code and arguments
	 *
	 * @param errors The current errors collection
	 * @param errorCode The given error code
	 * @param arguments The given arguments
	 */
	protected void addCustomError(Errors errors, String errorCode, Object[] arguments) {
		errors.reject(errorCode, arguments, null);
	}

	protected void shouldNotNullOrEmpty(String termName, String fieldName, Object value, Errors errors) {
		if (!isNullOrEmpty(value)) {
			return;
		}
		this.addCustomError(errors, fieldName, BaseValidator.FIELD_IS_REQUIRED, new Object[] {termName});
	}

	/**
	 * Adds the default error message into the current errors collection
	 *
	 * @param errors The current errors collection
	 */
	protected void addDefaultError(Errors errors) {
		errors.reject("unknown.error");
	}

	protected Throwable getRootCause(final Throwable cause) {
		Throwable rootCause = cause;
		while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
			rootCause = rootCause.getCause();
		}
		return rootCause;
	}

}
