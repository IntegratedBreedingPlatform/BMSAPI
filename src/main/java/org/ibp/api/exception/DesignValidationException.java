package org.ibp.api.exception;

public class DesignValidationException extends Exception {

	private static final long serialVersionUID = -5262966560650957460L;
	private String errorCode;
	private String labelError;

	public DesignValidationException(String message) {
		super(message);
	}

	public DesignValidationException(String errorCode, String labelError, String message) {
		super(message);
		this.errorCode = errorCode;
		this.labelError = labelError;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getLabelError() {
		return this.labelError;
	}

	public void setLabelError(String labelError) {
		this.labelError = labelError;
	}


}
