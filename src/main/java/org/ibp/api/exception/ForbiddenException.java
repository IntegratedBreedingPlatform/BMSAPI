package org.ibp.api.exception;

import org.springframework.validation.ObjectError;

public class ForbiddenException extends RuntimeException {


	private static final long serialVersionUID = 1L;

	private final ObjectError error;

	public ForbiddenException(ObjectError error) {
		this.error = error;
	}

	public ObjectError getError() {
		return this.error;
	}

}
