package org.ibp.api.brapi.v1.search;

public abstract class SearchRequestDto {

	private Integer requestId;
	private SearchRequestType requestType;
	private String parameters;

	public Integer getRequestId() {
		return this.requestId;
	}

	public void setRequestId(final Integer requestId) {
		this.requestId = requestId;
	}

	public SearchRequestType getRequestType() {
		return this.requestType;
	}

	public void setRequestType(final SearchRequestType requestType) {
		this.requestType = requestType;
	}

	public String getParameters() {
		return this.parameters;
	}

	public void setParameters(final String parameters) {
		this.parameters = parameters;
	}
}
