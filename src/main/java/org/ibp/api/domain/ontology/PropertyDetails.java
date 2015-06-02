
package org.ibp.api.domain.ontology;

import java.util.Set;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Holds all property details. Extended from {@link TermSummary} for basic term details.
 */
public class PropertyDetails extends TermSummary {

	private String cropOntologyId;
	private Set<String> classes;
	private MetadataDetails metadata = new MetadataDetails();

	public String getCropOntologyId() {
		return this.cropOntologyId;
	}

	public void setCropOntologyId(String cropOntologyId) {
		this.cropOntologyId = cropOntologyId;
	}

	public Set<String> getClasses() {
		return this.classes;
	}

	public void setClasses(Set<String> classes) {
		this.classes = classes;
	}

	public MetadataDetails getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataDetails metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "PropertyDetails{" + "cropOntologyId='" + cropOntologyId + '\'' + ", classes=" + classes + ", metadata=" + metadata + "} "
				+ super.toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof PropertyDetails))
			return false;
		PropertyDetails castOther = (PropertyDetails) other;
		return new EqualsBuilder().append(cropOntologyId, castOther.cropOntologyId).append(classes, castOther.classes)
				.append(metadata, castOther.metadata).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(cropOntologyId).append(classes).append(metadata).toHashCode();
	}

}
