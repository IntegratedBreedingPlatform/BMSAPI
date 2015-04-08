package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PropertyRequest {

	@JsonIgnore
	private Integer id;

	private String name;
	private String description;
	private String cropOntologyId;
	private List<String> classes;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
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

	public List<String> getClasses() {
		if (this.classes == null) {
			this.classes = new ArrayList<>();
		}
		return this.classes;
	}

	public void setClasses(List<String> classes) {
		this.classes = classes;
	}

	public void setClassesFromSet(Set<String> classes) {
		if (classes == null) {
			return;
		}

		this.classes = new ArrayList<>(classes);
	}

	@Override
	public String toString() {
		return "Property [id=" + this.id + ", name='" + this.name + '\'' + ", description='"
				+ this.description + '\'' + ", cropOntologyId='" + this.cropOntologyId + '\''
				+ ", classes=" + this.classes + ']';
	}
}
