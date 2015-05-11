
package org.ibp.api.domain.ontology;

public class MethodSummary extends TermSummary {

	private MetadataSummary metadata = new MetadataSummary();

	public MetadataSummary getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataSummary metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "MethodSummary{} " + super.toString();
	}
}
