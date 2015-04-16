package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import java.util.Collection;
import java.util.Map;

/**
 * Helper methods to manage message codes.
 */
public abstract class BaseValidator {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String INVALID_ID = "invalid.id";
    protected static final String SHOULD_NOT_NULL_OR_EMPTY = "should.not.be.null";

    protected boolean isNonNullValidNumericString(Object value) {
        return value != null && (value instanceof Integer || value instanceof String && ((String) value).matches("^[0-9]+$"));
    }

    protected Integer getIntegerValueSafe(Object value, Integer defaultValue){
        if(value instanceof Integer){
            return (Integer) value;
        }

        if(value instanceof String){
            return Integer.valueOf((String) value);
        }

        return defaultValue;
    }

    protected void checkNumberFieldAndLength(String value, Errors errors){
        if(value.matches("^[0-9]+$")){
            try{
                Integer.valueOf(value);
                return;
            }catch (Exception ignored) {
                addCustomError(errors, INVALID_ID, null);
                return;
            }
        }
        addCustomError(errors, INVALID_ID, null);
    }

    /**
     * This function is useful to checking object value as null or empty with any plain object or from collection
     * @param value value of object
     * @return boolean
     */
    @SuppressWarnings("rawtypes")
	protected boolean isNullOrEmpty(Object value){
        return (value instanceof String && Strings.isNullOrEmpty(((String) value).trim())) ||
                value == null ||
                (value instanceof Collection && ((Collection) value).isEmpty()) ||
                (value instanceof Map && ((Map) value).isEmpty());
    }

    /**
     * Adds an custom error for the given field name, error code and arguments
     *
     * @param errors The current errors collection
     * @param fieldName The given field name
     * @param errorCode The given error code
     * @param arguments The given arguments
     */
    protected void addCustomError(Errors errors, String fieldName, String errorCode, Object[] arguments){
        errors.rejectValue(fieldName, errorCode, arguments, null);
    }

    /**
     * Adds an custom error for the given error code and arguments
     *
     * @param errors The current errors collection
     * @param errorCode The given error code
     * @param arguments The given arguments
     */
    protected void addCustomError(Errors errors, String errorCode, Object[] arguments){
        errors.reject(errorCode, arguments, null);
    }

    protected void shouldNotNullOrEmpty(String fieldName, Object value, Errors errors){
        if(!isNullOrEmpty(value)){
            return;
        }
        addCustomError(errors, fieldName, SHOULD_NOT_NULL_OR_EMPTY, new Object[]{fieldName});
    }

    protected void checkIntegerNull(String fieldName, Integer value, Errors errors){
        if(value == null){
            addCustomError(errors,fieldName, SHOULD_NOT_NULL_OR_EMPTY, null);
        }
    }

    /**
     * Adds the default error message into the current errors collection
     *
     * @param errors The current errors collection
     */
    protected void addDefaultError(Errors errors) {
        errors.reject("error.standard.defaultMessage");
    }

    protected void logError(final Throwable cause){
        Throwable rootCause = cause;
        while(rootCause.getCause() != null &&  rootCause.getCause() != rootCause){
            rootCause = rootCause.getCause();
        }

        log.error(String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(), rootCause.getStackTrace()[0].getMethodName()), cause);
    }

}
