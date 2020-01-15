
package org.ibp.api.exception;

public class BreedingViewInvalidFormatException extends Exception {

	private static final long serialVersionUID = 7052896286691168717L;

	public BreedingViewInvalidFormatException() {
		super("Error with parsing the breeding view output file.");
	}

	public BreedingViewInvalidFormatException(String message) {
		super(message);
	}

	public BreedingViewInvalidFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
