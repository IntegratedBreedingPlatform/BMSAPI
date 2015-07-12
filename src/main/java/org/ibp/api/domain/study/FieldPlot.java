
package org.ibp.api.domain.study;

public class FieldPlot {

	private Integer repetitionNumber;

	private Integer entryNumber;

	private Integer plotNumber;

	private Integer observationUniqueIdentifier;

	private Integer datasetId;

	private Integer geolocationId;

	private boolean plotDeleted;

	
	/**
	 * @return the datasetId
	 */
	public Integer getDatasetId() {
		return datasetId;
	}

	
	/**
	 * @return the geolocationId
	 */
	public Integer getGeolocationId() {
		return geolocationId;
	}

	
	/**
	 * @return the plotDeleted
	 */
	public boolean isPlotDeleted() {
		return plotDeleted;
	}

	/**
	 * @return the repetitionNumber
	 */
	public Integer getRepetitionNumber() {
		return this.repetitionNumber;
	}

	/**
	 * @param repetitionNumber the repetitionNumber to set
	 */
	public void setRepetitionNumber(Integer repetitionNumber) {
		this.repetitionNumber = repetitionNumber;
	}

	/**
	 * @return the entryNumber
	 */
	public Integer getEntryNumber() {
		return this.entryNumber;
	}

	/**
	 * @param entryNumber the entryNumber to set
	 */
	public void setEntryNumber(Integer entryNumber) {
		this.entryNumber = entryNumber;
	}

	/**
	 * @return the plotNumber
	 */
	public Integer getPlotNumber() {
		return this.plotNumber;
	}

	/**
	 * @param plotNumber the plotNumber to set
	 */
	public void setPlotNumber(Integer plotNumber) {
		this.plotNumber = plotNumber;
	}

	/**
	 * @return the observationUniqueIdentifier
	 */
	public Integer getObservationUniqueIdentifier() {
		return this.observationUniqueIdentifier;
	}

	/**
	 * @param observationUniqueIdentifier the observationUniqueIdentifier to set
	 */
	public void setObservationUniqueIdentifier(Integer observationUniqueIdentifier) {
		this.observationUniqueIdentifier = observationUniqueIdentifier;
	}

	public void setDatasetId(Integer datasetId) {
		this.datasetId = datasetId;

	}

	public void setGeolocationId(Integer geolocationId) {
		this.geolocationId = geolocationId;

	}

	public void setPlotDeleted(boolean plotDeleted) {
		this.plotDeleted = plotDeleted;
	}

}
