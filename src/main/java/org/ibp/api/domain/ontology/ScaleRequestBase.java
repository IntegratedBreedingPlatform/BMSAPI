package org.ibp.api.domain.ontology;

public class ScaleRequestBase {

	private String name;
	private String description;
	private String dataTypeId;
	private ValidValues validValues;

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

	public String getDataTypeId() {
		return this.dataTypeId;
	}

	public void setDataTypeId(String dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	public ValidValues getValidValues() {
		return this.validValues;
	}

	public void setValidValues(ValidValues validValues) {
		this.validValues = validValues;
	}

	@Override
	public String toString() {
		return "ScaleRequestBase{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", dataTypeId=" + dataTypeId +
				", validValues=" + validValues +
				'}';
	}
}
