
package org.ibp.api.ibpworkbench.exceptions;

public class BreedingViewImportException extends Exception {

	private static final long serialVersionUID = -1639961960516233500L;

	public BreedingViewImportException() {
		super("Error with importing breeding view output file.");
	}

	public BreedingViewImportException(String message) {
		super(message);
	}

	public BreedingViewImportException(String message, Throwable cause) {
		super(message, cause);
	}
}
