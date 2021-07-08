
package org.ibp.api.exception;

import org.springframework.validation.ObjectError;

import java.util.List;

public class ApiValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final Object[] params;

	// TODO merge with ApiRequestValidationException, add another constructor there
	public ApiValidationException(final String logMessage, final String errorCode, final Object... params) {
		super(logMessage);
		this.errorCode = errorCode;
		this.params = params;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public Object[] getParams() {
		return this.params;
	}
}
