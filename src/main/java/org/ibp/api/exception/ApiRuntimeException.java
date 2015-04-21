
package org.ibp.api.exception;

/**
 * For now just a marker class for use within API service interfaces to raise runtime issues that clients of the API can't do much about.
 *
 */
public class ApiRuntimeException extends RuntimeException {
	
	public ApiRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
