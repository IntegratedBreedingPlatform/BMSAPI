package org.ibp.api.domain.ontology;

public class ScaleRequest extends ScaleRequestBase {

	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ScaleRequest{" +
				"id=" + id +
				"} " + super.toString();
	}
}


