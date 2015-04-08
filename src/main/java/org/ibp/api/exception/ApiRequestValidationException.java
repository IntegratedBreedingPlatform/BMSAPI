package org.ibp.api.exception;

import org.springframework.validation.ObjectError;

import java.util.List;

public class ApiRequestValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    private final List<ObjectError> errors;

    public ApiRequestValidationException(List<ObjectError> errors){
        this.errors = errors;
    }

    public List<ObjectError> getErrors() {
        return this.errors;
    }
}
