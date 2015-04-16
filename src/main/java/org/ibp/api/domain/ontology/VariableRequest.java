package org.ibp.api.domain.ontology;

public class VariableRequest extends UpdateVariableRequest {

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "VariableRequest{" +
				"id=" + id +
				"} " + super.toString();
	}
}
