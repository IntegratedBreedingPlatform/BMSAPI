
package org.ibp.api.domain.ontology;


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
}
