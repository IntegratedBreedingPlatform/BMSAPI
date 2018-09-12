package org.ibp.api.rest.samplesubmission.domain;

import com.sun.tools.javac.util.List;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;


@AutoProperty
public class Header {

	@AutoProperty
	class Status {

		private Boolean succeded;

		private List<Object> statusMessages;

		public Boolean getSucceded() {
			return succeded;
		}

		public void setSucceded(final Boolean succeded) {
			this.succeded = succeded;
		}

		public List<Object> getStatusMessages() {
			return statusMessages;
		}

		public void setStatusMessages(final List<Object> statusMessages) {
			this.statusMessages = statusMessages;
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
		public boolean equals(Object o) {
			return Pojomatic.equals(this, o);
		}

	}

	private String gobiiProcessType;

	private GOBiiToken dtoHeaderAuth;

	//Confirm type
	private Object pagination;

	private String gobiiVersion;

	private String cropType;

	private Status status;

	private Object statusMessagesByCode;

	public String getGobiiProcessType() {
		return gobiiProcessType;
	}

	public void setGobiiProcessType(final String gobiiProcessType) {
		this.gobiiProcessType = gobiiProcessType;
	}

	public GOBiiToken getDtoHeaderAuth() {
		return dtoHeaderAuth;
	}

	public void setDtoHeaderAuth(final GOBiiToken dtoHeaderAuth) {
		this.dtoHeaderAuth = dtoHeaderAuth;
	}

	public Object getPagination() {
		return pagination;
	}

	public void setPagination(final Object pagination) {
		this.pagination = pagination;
	}

	public String getGobiiVersion() {
		return gobiiVersion;
	}

	public void setGobiiVersion(final String gobiiVersion) {
		this.gobiiVersion = gobiiVersion;
	}

	public String getCropType() {
		return cropType;
	}

	public void setCropType(final String cropType) {
		this.cropType = cropType;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public Object getStatusMessagesByCode() {
		return statusMessagesByCode;
	}

	public void setStatusMessagesByCode(final Object statusMessagesByCode) {
		this.statusMessagesByCode = statusMessagesByCode;
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
