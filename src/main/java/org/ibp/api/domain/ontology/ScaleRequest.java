package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScaleRequest {

	@JsonIgnore
	private Integer id;

	private String name;
	private String description;
	private Integer dataTypeId;
	private ValidValues validValues;

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

	public Integer getDataTypeId() {
		return this.dataTypeId;
	}

	public void setDataTypeId(Integer dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	public ValidValues getValidValues() {
		return this.validValues;
	}

	public void setValidValues(ValidValues validValues) {
		this.validValues = validValues;
	}
}
