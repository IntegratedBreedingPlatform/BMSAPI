package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.base.Strings;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.common.ValidationErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public abstract class RequestValidatorHelper {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String UNKNOWN_ERROR = "unknown.error";
    protected static final String DATA_PROVIDER_FAILURE = "data.provider.failure";
    protected static final String INVALID_ID = "id.is.invalid.format";
    protected static final String INVALID_TYPE_ID = "type.id.is.invalid.format";
    protected static final String ID_DOES_NOT_EXIST = "id.does.not.exist";
    protected static final String FIELD_IS_REQUIRED = "field.is.required";
    protected static final String FIELD_SHOULD_BE_NUMERIC = "field.should.be.numeric";
    protected static final String FIELD_SHOULD_BE_STRING = "field.should.be.string";
    protected static final String TEXTUAL_FIELD_IS_TOO_LONG = "textual.field.is.too.long";
    protected static final String LIST_TEXTUAL_FIELD_IS_TOO_LONG = "list.textual.field.is.too.long";
    protected static final String NAME_ALREADY_EXIST = "name.already.exist";
    protected static final String RECORD_IS_NOT_EDITABLE = "record.is.not.editable";
    protected static final String RECORD_IS_NOT_DELETABLE = "record.is.not.deletable";
    protected static final String LIST_SHOULD_NOT_BE_EMPTY = "list.should.not.be.empty";
    protected static final String MIN_MAX_NOT_VALID = "min.max.not.valid";
    protected static final String MIN_SHOULD_NOT_GREATER_THEN_MAX = "min.should.not.be.greater.than.max";

    protected void addMiddlewareError(MiddlewareException exception, ValidationErrors validationErrors) {
        validationErrors.addError(DATA_PROVIDER_FAILURE);
    }

    protected void checkNumberField(String value, ValidationErrors errors){
        if(value.matches("^[0-9]+$")){
            try{
                Integer.valueOf(value);
            }catch (NumberFormatException ignored) {
                errors.addError(INVALID_ID);
            }
        }
    }

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

    protected void shouldNotNullOrEmpty(String termName, String fieldName, Object value, ValidationErrors errors){
        if(!isNullOrEmpty(value)){
            return;
        }
        errors.addError(FIELD_IS_REQUIRED, fieldName, termName);
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

    protected void logError(final Throwable cause){
        Throwable rootCause = cause;
        while(rootCause.getCause() != null &&  rootCause.getCause() != rootCause){
            rootCause = rootCause.getCause();
        }

        log.error(String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(), rootCause.getStackTrace()[0].getMethodName()), cause);
    }
}
