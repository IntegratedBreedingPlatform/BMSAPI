package org.ibp.api.exception;

public class BVLicenseParseException extends BVDesignException {

	private String messageFromApp;

	public BVLicenseParseException(final String errorMessage) {
		super(errorMessage);
	}

	public BVLicenseParseException(final String bvErrorCode, final String messageFromApp) {
		super(bvErrorCode);
		this.messageFromApp = messageFromApp;
	}

	public String getMessageFromApp() {
		return messageFromApp;
	}
}
