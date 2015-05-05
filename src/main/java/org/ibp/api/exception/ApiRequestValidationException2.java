package org.ibp.api.exception;

import org.ibp.api.domain.common.ValidationErrors;

public class ApiRequestValidationException2 extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ValidationErrors errors;

	public ApiRequestValidationException2(ValidationErrors errors) {
		this.errors = errors;
	}

	public ValidationErrors getValidationObject() {
		return this.errors;
	}
}
