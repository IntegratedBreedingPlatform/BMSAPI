package org.ibp.api.domain.ontology;

public class MethodSummary extends MethodRequest {

	private Integer id;

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Method [id=" + this.getId() + ", name=" + this.getName() + ", description="
				+ this.getDescription() + "]";
	}
}