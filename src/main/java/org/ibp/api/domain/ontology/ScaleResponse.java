package org.ibp.api.domain.ontology;

import java.util.List;

public class ScaleResponse extends ScaleSummary implements EditableDeletableFields{

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
        return "Scale  [id=" + this.getId()
                + ", name=" + this.getName()
                + ", description=" + this.getDescription()
                + ", dataType=" + this.getDataType()
                + ", editableFields=" + this.editableFields
                + ", deletable=" + this.deletable
                + ']';
    }
}
