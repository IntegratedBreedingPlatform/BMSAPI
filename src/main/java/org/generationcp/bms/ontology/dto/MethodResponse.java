package org.generationcp.bms.ontology.dto;

import java.util.List;

public class MethodResponse extends MethodSummary {

    private List<String> editableFields;
    
    private Boolean deletable;

    public List<String> getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(List<String> editableFields) {
        this.editableFields = editableFields;
    }

    public Boolean getDeletable() {
        return deletable;
    }

    public void setDeletable(Boolean deletable) {
        this.deletable = deletable;
    }
    
    public String toString() {
        return "Method [id=" + this.getId() 
                + ", name=" + this.getName() 
                + ", description=" + this.getDescription() 
                + ", editableFields=" + this.editableFields.toString() 
                + ", deletable=" + this.getDeletable() + "]";
    }
}
