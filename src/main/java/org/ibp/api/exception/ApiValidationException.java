
package org.ibp.api.exception;

/**
 * TODO merge with {@link ApiRequestValidationException#ApiRequestValidationException(java.lang.String, java.lang.Object[])}
 */
public class ApiValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final Object[] params;

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
