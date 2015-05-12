
package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

public class MetadataDetails extends MetadataSummary {

	private List<String> editableFields = new ArrayList<>();
	private boolean deletable;
	private Usage usage = new Usage();

	public Usage getUsage() {
		return usage;
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}

	public List<String> getEditableFields() {
		return this.editableFields;
	}

	public void setEditableFields(List<String> editableFields) {
		this.editableFields = editableFields;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}
}
