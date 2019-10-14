package org.ibp.api.domain.design;

public class DesignLicenseInfo {

	private Status status;

	public Status getStatus ()
	{
		return status;
	}

	public void setStatus (Status status)
	{
		this.status = status;
	}

	@Override
	public String toString()
	{
		return "DesignLicenseInfo [status = "+status+"]";
	}

}
