
package org.ibp.api.domain.study;

import java.util.List;

public class Observation {

	private Integer germplasmId;
	private String germplasmDesignation;
	private Integer enrtyNumber;
	private String entryType;
	private Integer plotNumber;
	private String parentage;
	private Integer replicationNumber;
	private Integer environmentNumber;

	private List<Measurement> measurements;

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

	public Integer getEnrtyNumber() {
		return enrtyNumber;
	}

	public void setEnrtyNumber(Integer enrtyNumber) {
		this.enrtyNumber = enrtyNumber;
	}

	
	public String getEntryType() {
		return entryType;
	}

	
	public void setEntryType(String entryType) {
		this.entryType = entryType;
	}

	public Integer getPlotNumber() {
		return plotNumber;
	}

	public void setPlotNumber(Integer plotNumber) {
		this.plotNumber = plotNumber;
	}

	public String getParentage() {
		return parentage;
	}

	public void setParentage(String parentage) {
		this.parentage = parentage;
	}

	public Integer getReplicationNumber() {
		return replicationNumber;
	}

	public void setReplicationNumber(Integer replicationNumber) {
		this.replicationNumber = replicationNumber;
	}

	public Integer getEnvironmentNumber() {
		return environmentNumber;
	}

	public void setEnvironmentNumber(Integer environmentNumber) {
		this.environmentNumber = environmentNumber;
	}

	public List<Measurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}

}
