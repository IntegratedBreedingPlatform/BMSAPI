package org.ibp.api.exception;

import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Created by clarysabel on 10/30/18.
 */
public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final List<ObjectError> errors;

	public ConflictException(List<ObjectError> errors) {
		this.errors = errors;
	}

	public List<ObjectError> getErrors() {
		return this.errors;
	}
}

