
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class MethodSummary extends TermSummary {

	private MetadataSummary metadata = new MetadataSummary();

	public MetadataSummary getMetadata() {
		return this.metadata;
	}

	public void setMetadata(MetadataSummary metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "MethodSummary{} " + super.toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof MethodSummary)) {
			return false;
		}
		MethodSummary castOther = (MethodSummary) other;
		return new EqualsBuilder().append(this.metadata, castOther.metadata).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.metadata).toHashCode();
	}

}
