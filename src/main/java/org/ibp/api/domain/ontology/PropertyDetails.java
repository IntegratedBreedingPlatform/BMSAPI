package org.ibp.api.domain.ontology;

import java.util.Set;

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
		return "PropertyDetails{" +
				"cropOntologyId='" + cropOntologyId + '\'' +
				", classes=" + classes +
				", metadata=" + metadata +
				"} " + super.toString();
	}
}

