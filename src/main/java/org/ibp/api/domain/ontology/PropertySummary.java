package org.ibp.api.domain.ontology;

import java.util.Set;

public class PropertySummary extends AuditTermSummary {

	private String cropOntologyId;
	private Set<String> classes;

	public String getCropOntologyId() {
		return this.cropOntologyId;
	}

	public void setCropOntologyId(String cropOntologyId) {
		this.cropOntologyId = cropOntologyId;
	}

	public Set<String> getClasses() {
		return this.classes;
	}

	public void setClasses(Set<String> classes) {
		this.classes = classes;
	}

	@Override
	public String toString() {
		return "PropertySummary{" +
				"cropOntologyId='" + cropOntologyId + '\'' +
				", classes=" + classes +
				"} " + super.toString();
	}
}
