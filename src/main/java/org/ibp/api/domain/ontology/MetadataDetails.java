
package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

/**
 * Derived from {@link MetadataSummary} to provide extra information like fields which are editable, record is deletable
 * and variable usage. {@link Usage} only have observations and studies carried out for variable
 */
public class MetadataDetails extends MetadataSummary {

	private final List<String> editableFields = new ArrayList<>();
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

	public void addEditableField(String editableField) {
		this.editableFields.add(editableField);
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public void setObservations(Integer observations){
		this.usage.setObservations(observations);
	}

	public void setStudies(Integer studies){
		this.usage.setStudies(studies);
	}
}
