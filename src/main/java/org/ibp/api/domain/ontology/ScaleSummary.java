package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * Contains basic data used for list, insert and update of scale
 * Extended from {@link TermSummary} for getting basic fields like id, name and description
 */
public class ScaleSummary extends TermSummary {

	private DataType dataType;

	private final ValidValues validValues = new ValidValues();
	private MetadataSummary metadata = new MetadataSummary();

	public DataType getDataType() {
		return this.dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public ValidValues getValidValues() {
		return this.validValues;
	}

	public void setMinValue(Integer minValue) {
		this.validValues.setMin(minValue);
	}

	public void setMaxValue(Integer maxValue) {
		this.validValues.setMax(maxValue);
	}

	@JsonIgnore
	public void setCategories(Map<String, String> categories) {
		this.validValues.setCategoriesFromMap(categories);
	}

	public MetadataSummary getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataSummary metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "ScaleSummary{" +
				"dataType=" + dataType +
				", validValues=" + validValues +
				", metadata=" + metadata +
				"} " + super.toString();
	}
}
