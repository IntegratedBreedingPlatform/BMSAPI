package org.ibp.api.domain.ontology;

public class VariableUsages {

	private Integer observations;
	private Integer studies;

	public Integer getObservations() {
		return observations;
	}

	public void setObservations(Integer observations) {
		this.observations = observations;
	}

	public Integer getStudies() {
		return studies;
	}

	public void setStudies(Integer studies) {
		this.studies = studies;
	}

	@Override
	public String toString() {
		return "VariableUsages{" +
				"observations=" + observations +
				", studies=" + studies +
				'}';
	}
}
