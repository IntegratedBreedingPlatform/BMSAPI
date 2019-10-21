package org.ibp.api.domain.design;

public class Status {

	private String returnCode;

	private String appStatus;

	private License license;

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getAppStatus() {
		return appStatus;
	}

	public void setAppStatus(String appStatus) {
		this.appStatus = appStatus;
	}

	public License getLicense() {
		return license;
	}

	public void setLicense(License license) {
		this.license = license;
	}

	@Override
	public String toString() {
		return "Status [returnCode = " + returnCode + ", appStatus = " + appStatus + ", license = " + license + "]";
	}
}
