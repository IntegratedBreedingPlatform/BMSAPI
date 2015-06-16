
package org.ibp.api.domain.ontology;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Contains basic data used for list, insert and update of scale Extended from {@link TermSummary} for getting basic fields like id, name
 * and description
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

	public void setMin(String min) {
		this.validValues.setMin(min);
	}

	public void setMax(String max) {
		this.validValues.setMax(max);
	}

	@JsonIgnore
	public void setCategories(Map<String, String> categories) {
		this.validValues.setCategoriesFromMap(categories);
	}

	public MetadataSummary getMetadata() {
		return this.metadata;
	}

	public void setMetadata(MetadataSummary metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "ScaleSummary{" + "dataType=" + this.dataType + ", validValues=" + this.validValues + ", metadata=" + this.metadata + "} "
				+ super.toString();
	}
}
