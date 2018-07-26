package org.ibp.api.exception;

public class InvalidValuesException extends Exception {

	public InvalidValuesException(String message) {
		super(message);
	}

	public InvalidValuesException(String message, Throwable cause) {
		super(message, cause);
	}

}
