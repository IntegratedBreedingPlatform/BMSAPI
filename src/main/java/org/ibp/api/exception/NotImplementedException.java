package org.ibp.api.exception;

import org.springframework.validation.ObjectError;

/**
 * Created by clarysabel on 10/30/18.
 */
public class NotImplementedException extends RuntimeException {


	private static final long serialVersionUID = 1L;

	private final ObjectError error;

	public NotImplementedException(ObjectError error) {
		this.error = error;
	}

	public ObjectError getError() {
		return this.error;
	}

}
