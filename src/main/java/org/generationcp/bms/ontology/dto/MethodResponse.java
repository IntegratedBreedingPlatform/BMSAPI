package org.generationcp.bms.ontology.dto;

import java.util.List;

public class MethodResponse extends MethodSummary implements EditableDeletableFields {

    private List<String> editableFields;

    private Boolean deletable;

    @Override
    public List<String> getEditableFields() {
        return editableFields;
    }

    @Override
    public void setEditableFields(List<String> editableFields) {
        this.editableFields = editableFields;
    }

    @Override
    public Boolean getDeletable() {
        return deletable;
    }

    @Override
    public void setDeletable(Boolean deletable) {
        this.deletable = deletable;
    }

    @Override
    public String toString() {
        return "Method [id=" + this.getId() 
                + ", name=" + this.getName() 
                + ", description=" + this.getDescription() 
                + ", editableFields=" + this.editableFields.toString() 
                + ", deletable=" + this.getDeletable() + "]";
    }
}
