
package org.ibp.api.exception;

/**
 * TODO migrate {@link ApiRuntimeException} and rename this to ApiRuntimeException
 */
public class ApiRuntime2Exception extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final Object[] params;

	public ApiRuntime2Exception(final String logMessage, final String errorCode, final Object... params) {
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
