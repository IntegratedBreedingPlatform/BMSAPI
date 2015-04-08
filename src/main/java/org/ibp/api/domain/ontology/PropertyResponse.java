package org.ibp.api.domain.ontology;

import java.util.List;

public class PropertyResponse extends PropertySummary implements EditableDeletableFields {

	private List<String> editableFields;

	private Boolean deletable;

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
		return "Property [id=" + this.getId() + ", name=" + this.getName() + ", description="
				+ this.getDescription() + ", cropOntologyId='" + this.getCropOntologyId()
				+ ", classes=" + this.getClasses() + ", editableFields="
				+ this.editableFields.toString() + ", deletable=" + this.getDeletable() + "]";
	}
}
