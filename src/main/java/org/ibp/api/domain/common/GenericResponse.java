package org.ibp.api.domain.common;

public class GenericResponse {

	private int id;

	public GenericResponse(int id) {
		this.setId(id);
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
