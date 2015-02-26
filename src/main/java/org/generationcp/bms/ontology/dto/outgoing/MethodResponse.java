package org.generationcp.bms.ontology.dto.outgoing;

import java.util.ArrayList;
import java.util.List;

public class MethodResponse extends MethodSummary {

    private List<String> editableFields;

    public List<String> getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(List<String> editableFields) {
        this.editableFields = editableFields;
    }
    
    private void addEditableFiled(String fieldName){
        if(this.editableFields == null) this.editableFields = new ArrayList<>();
        this.editableFields.add(fieldName);
    }

    private Boolean deletable;

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
