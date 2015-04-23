
package org.ibp.api.domain.study;

import java.util.List;

public class Observation {

	private String uniqueIdentifier;
	private Integer germplasmId;
	private String germplasmDesignation;
	private String enrtyNumber;
	private String entryType;
	private String plotNumber;
	private String replicationNumber;
	private String environmentNumber;

	private List<Measurement> measurements;
	
	/**
	 * @return the uniqueIdentifier
	 */
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	/**
	 * @param uniqueIdentifier the uniqueIdentifier to set
	 */
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public Integer getGermplasmId() {
		return germplasmId;
	}

	public void setGermplasmId(Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	public String getGermplasmDesignation() {
		return germplasmDesignation;
	}

	public void setGermplasmDesignation(String germplasmDesignation) {
		this.germplasmDesignation = germplasmDesignation;
	}

	public String getEnrtyNumber() {
		return enrtyNumber;
	}

	public void setEnrtyNumber(String enrtyNumber) {
		this.enrtyNumber = enrtyNumber;
	}

	
	public String getEntryType() {
		return entryType;
	}

	
	public void setEntryType(String entryType) {
		this.entryType = entryType;
	}

	public String getPlotNumber() {
		return plotNumber;
	}

	public void setPlotNumber(String plotNumber) {
		this.plotNumber = plotNumber;
	}

	public String getReplicationNumber() {
		return replicationNumber;
	}

	public void setReplicationNumber(String replicationNumber) {
		this.replicationNumber = replicationNumber;
	}

	public String getEnvironmentNumber() {
		return environmentNumber;
	}

	public void setEnvironmentNumber(String environmentNumber) {
		this.environmentNumber = environmentNumber;
	}

	public List<Measurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}

}
