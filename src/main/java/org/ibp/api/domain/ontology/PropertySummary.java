package org.ibp.api.domain.ontology;

import java.util.Set;

public class PropertySummary  {

	private String id;
	private String name;
	private String description;
	private String cropOntologyId;
	private Set<String> classes;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

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
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", cropOntologyId='" + cropOntologyId + '\'' +
				", classes=" + classes +
				'}';
	}
}
