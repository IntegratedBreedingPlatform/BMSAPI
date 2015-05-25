package org.ibp.api.domain.ontology;

import java.util.Map;

/**
 * Holds all Scale details. Extended from {@link TermSummary} for basic term details.
 */
public class ScaleDetails extends TermSummary {

	private DataType dataType;

	private final ValidValues validValues = new ValidValues();
	private MetadataDetails metadata = new MetadataDetails();

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

	public void setCategories(Map<String, String> categories) {
		this.validValues.setCategoriesFromMap(categories);
	}

	public MetadataDetails getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataDetails metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "ScaleDetails{" +
				"dataType=" + dataType +
				", validValues=" + validValues +
				", metadata=" + metadata +
				"} " + super.toString();
	}
}
