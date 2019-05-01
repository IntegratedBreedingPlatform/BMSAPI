package org.ibp.api.exception;

import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Created by clarysabel on 11/27/18.
 */
public class PreconditionFailedException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	private final List<ObjectError> errors;

	public PreconditionFailedException(List<ObjectError> errors) {
		this.errors = errors;
	}

	public List<ObjectError> getErrors() {
		return this.errors;
	}

}
