
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class MethodDetails extends TermSummary {

	private MetadataDetails metadata = new MetadataDetails();

	public MetadataDetails getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataDetails metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "Method [id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + "]";
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof MethodDetails))
			return false;
		MethodDetails castOther = (MethodDetails) other;
		return new EqualsBuilder().append(metadata, castOther.metadata).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(metadata).toHashCode();
	}

}
