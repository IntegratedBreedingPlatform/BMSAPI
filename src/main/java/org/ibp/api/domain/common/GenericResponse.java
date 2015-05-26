package org.ibp.api.domain.common;

public class GenericResponse {

	private String id;

	public GenericResponse(String id) {
		this.setId(id);
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
