
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.List;

public class FieldMapMetaData {

	private List<FieldMapStudySummary> relevantStudies = new ArrayList<>();

	private FieldMapPlantingDetails fieldPlantingDetails;

	
	public List<FieldMapStudySummary> getRelevantStudies() {
		return relevantStudies;
	}

	public void setRelevantStudies(List<FieldMapStudySummary> relevantStudies) {
		this.relevantStudies = relevantStudies;
	}

	public FieldMapPlantingDetails getFieldPlantingDetails() {
		return this.fieldPlantingDetails;
	}

	public void setFieldPlantingDetails(FieldMapPlantingDetails fieldPlantingDetails) {
		this.fieldPlantingDetails = fieldPlantingDetails;
	}

}
