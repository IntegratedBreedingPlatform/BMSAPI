package org.ibp.api.java.impl.middleware.design.validator;

public class ExperimentDesignValidationOutput {

	private static final long serialVersionUID = -2261802820353959484L;
	private boolean isValid;
	private String message;
	private boolean userConfirmationRequired;

	public ExperimentDesignValidationOutput() {
		super();
	}

	public ExperimentDesignValidationOutput(boolean isValid, String message) {
		super();
		this.isValid = isValid;
		this.message = message;
	}

	public boolean isValid() {
		return this.isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isUserConfirmationRequired() {
		return userConfirmationRequired;
	}

	public void setUserConfirmationRequired(boolean userConfirmationRequired) {
		this.userConfirmationRequired = userConfirmationRequired;
	}

}
