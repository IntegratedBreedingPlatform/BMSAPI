
package org.ibp.api.exception;

public class BreedingViewImportException extends Exception {

	private static final long serialVersionUID = -1639961960516233500L;

	public BreedingViewImportException() {
		super("Error with importing breeding view output file.");
	}

	public BreedingViewImportException(final String message) {
		super(message);
	}

	public BreedingViewImportException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
