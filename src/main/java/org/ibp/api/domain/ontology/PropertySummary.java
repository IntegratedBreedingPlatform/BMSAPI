package org.ibp.api.domain.ontology;

public class PropertySummary extends PropertyRequest {

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
		return "Property [" + "id=" + this.getId() + ", name='" + this.getName() + '\''
				+ ", description='" + this.getDescription() + '\'' + ", cropOntologyId='"
				+ this.getCropOntologyId() + '\'' + ", classes=" + this.getClasses() + ']';
	}
}
