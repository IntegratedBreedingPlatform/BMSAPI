package org.ibp.api.rest.cop;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class COPPermissionsResponce {

	private String message;
	private String status;
	private String eid;
	private String email_sent;

	public COPPermissionsResponce() {

	}

	public String getMessage() {
		return message;
	}

	public COPPermissionsResponce setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public COPPermissionsResponce setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getEid() {
		return eid;
	}

	public COPPermissionsResponce setEid(String eid) {
		this.eid = eid;
		return this;
	}

	public String getEmail_sent() {
		return email_sent;
	}

	public COPPermissionsResponce setEmail_sent(String email_sent) {
		this.email_sent = email_sent;
		return this;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
