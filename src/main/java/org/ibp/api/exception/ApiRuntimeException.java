
package org.ibp.api.exception;

/**
 * For now just a marker class for use within API service interfaces to raise runtime issues that clients of the API can't do much about.
 *
 */
public class ApiRuntimeException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -7991039875168381487L;

	public ApiRuntimeException(String message) {
		super(message);
	}

	public ApiRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
