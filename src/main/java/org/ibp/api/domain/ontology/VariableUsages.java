
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

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
		return "VariableUsages{" + "observations=" + observations + ", studies=" + studies + '}';
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof VariableUsages))
			return false;
		VariableUsages castOther = (VariableUsages) other;
		return new EqualsBuilder().append(observations, castOther.observations).append(studies, castOther.studies).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(observations).append(studies).toHashCode();
	}

}
