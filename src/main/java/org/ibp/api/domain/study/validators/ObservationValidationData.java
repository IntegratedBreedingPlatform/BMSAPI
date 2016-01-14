
package org.ibp.api.domain.study.validators;

import java.util.Map;

/**
 * Data structure that holds all data needed to validate a observation.
 */
public class ObservationValidationData {

	/**
	 * Crop that the observation is being carried out for.
	 */
	private String cropName;
	
	/**
	 * Program id of the observation 
	 */
	private String programId;
	
	
	/**
	 * Study id of the observation
	 */
	private String studyId;
	
	/**
	 * The actual observation id.
	 */
	private Integer observationId;
	
	/**
	 * Map of array index to measurement variable details.  
	 */
	private Map<Integer, MeasurementDetails> measurementVariableDetailsList;

	public ObservationValidationData(final String cropName, final String programId, final String studyId, final Integer observationId,
			final Map<Integer, MeasurementDetails> measurementVariableDetailsList) {
		this.cropName = cropName;
		this.programId = programId;
		this.studyId = studyId;
		this.observationId = observationId;
		this.measurementVariableDetailsList = measurementVariableDetailsList;
	}

	public String getCropName() {
		return this.cropName;
	}

	public String getProgramId() {
		return this.programId;
	}

	public String getStudyId() {
		return this.studyId;
	}

	public Integer getObservationId() {
		return this.observationId;
	}

	public Map<Integer, MeasurementDetails> getMeasurementVariableDetailsList() {
		return this.measurementVariableDetailsList;
	}

}
