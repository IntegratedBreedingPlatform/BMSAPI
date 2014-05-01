package org.generationcp.bms.domain;

public class TraitObservation {

	private final int experimentId;	
	private final int germplasmId;
    private final int environmentId;
	
	private String designation;
	private String value;

	public TraitObservation(int experimentId, int germplasmId, int environmentId) {
		this.experimentId = experimentId;
		this.germplasmId = germplasmId;
		this.environmentId = environmentId;	
	}

	public int getGermplasmId() {
		return germplasmId;
	}

	public int getEnvironmentId() {
		return environmentId;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getExperimentId() {
		return experimentId;
	}

}
