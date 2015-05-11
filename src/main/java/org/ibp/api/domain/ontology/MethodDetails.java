
package org.ibp.api.domain.ontology;

import java.util.List;

public class MethodDetails extends TermSummary implements EditableDeletableFields {

	private List<String> editableFields;

	private Boolean deletable;

	private MetadataDetails metadata = new MetadataDetails();

	public MetadataDetails getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataDetails metadata) {
		this.metadata = metadata;
	}

	@Override
	public List<String> getEditableFields() {
		return this.editableFields;
	}

	@Override
	public void setEditableFields(List<String> editableFields) {
		this.editableFields = editableFields;
	}

	@Override
	public Boolean getDeletable() {
		return this.deletable;
	}

	@Override
	public void setDeletable(Boolean deletable) {
		this.deletable = deletable;
	}

	@Override
	public String toString() {
		return "Method [id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", editableFields=" + this.editableFields.toString()
				+ ", deletable=" + this.getDeletable() + "]";
	}
}
