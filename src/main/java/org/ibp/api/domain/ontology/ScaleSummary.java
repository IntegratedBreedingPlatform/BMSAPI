package org.ibp.api.domain.ontology;

import java.util.Map;

public class ScaleSummary {

	private Integer id;
	private String name;
	private String description;
	private IdName dataType;

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

	public IdName getDataType() {
		return this.dataType;
	}

	public void setDataType(IdName dataType) {
		this.dataType = dataType;
	}

	public ValidValues getValidValues() {
		return this.validValues;
	}

	public void setMinValue(String minValue) {
		this.ensureValidValuesInitialized();
		this.validValues.setMin(minValue);
	}

	public void setMaxValue(String maxValue) {
		this.ensureValidValuesInitialized();
		this.validValues.setMax(maxValue);
	}

	public void setCategories(Map<String, String> categories) {
		this.ensureValidValuesInitialized();
		this.validValues.setCategoriesFromMap(categories);
	}

	private void ensureValidValuesInitialized() {
		if (this.validValues == null) {
			this.validValues = new ValidValues();
		}
	}

	@Override
	public String toString() {
		return "Scale  [id=" + this.getId() + ", name=" + this.getName() + ", description="
				+ this.getDescription() + ", dataType=" + this.getDataType() + ']';
	}
}
