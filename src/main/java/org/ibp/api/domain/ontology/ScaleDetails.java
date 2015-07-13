
package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.ibp.api.domain.ontology.serializers.ScaleDetailsSerializer;

import java.util.List;

/**
 * Holds all Scale details. Extended from {@link TermSummary} for basic term details.
 */

@JsonSerialize(using = ScaleDetailsSerializer.class)
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

	public void setMinValue(String minValue) {
		this.validValues.setMin(minValue);
	}

	public void setCategories(List<TermSummary> categories){
		this.validValues.setCategories(categories);
	}

	public void setMaxValue(String maxValue) {
		this.validValues.setMax(maxValue);
	}

	public MetadataDetails getMetadata() {
		return this.metadata;
	}

	public void setMetadata(MetadataDetails metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "ScaleDetails{" + "dataType=" + this.dataType + ", validValues=" + this.validValues + ", metadata=" + this.metadata + "} "
				+ super.toString();
	}
}
