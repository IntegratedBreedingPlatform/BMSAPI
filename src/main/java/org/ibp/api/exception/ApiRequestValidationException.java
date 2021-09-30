
package org.ibp.api.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.ObjectError;

public class ApiRequestValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final List<ObjectError> errors;

	public ApiRequestValidationException(List<ObjectError> errors) {
		this.errors = errors;
	}

	public ApiRequestValidationException(final String errorCode, final Object[] params) {
		this.errors = new ArrayList<>();
		this.errors.add(new ObjectError("", new String[] {errorCode}, params, ""));
	}

	public List<ObjectError> getErrors() {
		return this.errors;
	}
}
