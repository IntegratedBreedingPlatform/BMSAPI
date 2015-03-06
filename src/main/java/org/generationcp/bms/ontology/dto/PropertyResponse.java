package org.generationcp.bms.ontology.dto;

import java.util.List;

public class PropertyResponse extends PropertySummary {

    private List<String> editableFields;

    public List<String> getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(List<String> editableFields) {
        this.editableFields = editableFields;
    }

    private Boolean deletable;

    public Boolean getDeletable() {
        return deletable;
    }

    public void setDeletable(Boolean deletable) {
        this.deletable = deletable;
    }

    public String toString() {
        return "Property [id=" + this.getId()
                + ", name=" + this.getName()
                + ", description=" + this.getDescription()
                + ", cropOntologyId='" + this.getCropOntologyId()
                + ", classes=" + this.getClasses()
                + ", editableFields=" + this.editableFields.toString()
                + ", deletable=" + this.getDeletable() + "]";
    }
}
