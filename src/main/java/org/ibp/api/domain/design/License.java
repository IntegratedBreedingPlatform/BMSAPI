package org.ibp.api.domain.design;

public class License {

	private String message;

	private String expiryDays;

	private String expiry;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExpiryDays() {
		return expiryDays;
	}

	public void setExpiryDays(String expiryDays) {
		this.expiryDays = expiryDays;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	@Override
	public String toString() {
		return "License [message = " + message + ", expiryDays = " + expiryDays + ", expiry = " + expiry + "]";
	}
}
