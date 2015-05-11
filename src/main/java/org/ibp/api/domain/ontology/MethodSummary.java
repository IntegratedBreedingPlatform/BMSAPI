
package org.ibp.api.domain.ontology;

import java.util.Date;

public class MethodSummary extends TermSummary {

	private final MetadataSummary metadata = new MetadataSummary();

	public MetadataSummary getMetadata() {
		return metadata;
	}

	public void setDateCreated(Date dateCreated) {
		this.metadata.setDateCreated(dateCreated);
	}

	public void setDateLastModified(Date dateLastModified) {
		this.metadata.setDateLastModified(dateLastModified);
	}

	@Override
	public String toString() {
		return "MethodSummary{} " + super.toString();
	}
}
