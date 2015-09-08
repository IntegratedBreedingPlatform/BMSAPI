
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class VariableUsages {

	private Integer observations;
	private Integer studies;

	public Integer getObservations() {
		return this.observations;
	}

	public void setObservations(Integer observations) {
		this.observations = observations;
	}

	public Integer getStudies() {
		return this.studies;
	}

	public void setStudies(Integer studies) {
		this.studies = studies;
	}

	@Override
	public String toString() {
		return "VariableUsages{" + "observations=" + this.observations + ", studies=" + this.studies + '}';
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof VariableUsages)) {
			return false;
		}
		VariableUsages castOther = (VariableUsages) other;
		return new EqualsBuilder().append(this.observations, castOther.observations).append(this.studies, castOther.studies).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.observations).append(this.studies).toHashCode();
	}

}
