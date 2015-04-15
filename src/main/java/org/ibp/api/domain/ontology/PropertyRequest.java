package org.ibp.api.domain.ontology;

public class PropertyRequest extends PropertyRequestBase {

	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override public String toString() {
		return "PropertyRequest{" +
				"id='" + id + '\'' +
				"} " + super.toString();
	}
}

