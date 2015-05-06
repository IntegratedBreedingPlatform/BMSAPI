package org.ibp.api.domain.ontology;

import java.util.Map;

public class ScaleSummary extends AuditTermSummary {

	private IdName dataType;

	private final ValidValues validValues = new ValidValues();

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
		this.validValues.setMin(minValue);
	}

	public void setMaxValue(String maxValue) {
		this.validValues.setMax(maxValue);
	}

	public void setCategories(Map<String, String> categories) {
		this.validValues.setCategoriesFromMap(categories);
	}

	@Override
	public String toString() {
		return "ScaleSummary{" +
				"dataType=" + dataType +
				", validValues=" + validValues +
				"} " + super.toString();
	}
}
