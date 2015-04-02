package org.generationcp.bms.ontology.dto;

import java.util.List;

public interface EditableDeletableFields {

    List<String> getEditableFields();
    void setEditableFields(List<String> editableFields);
    Boolean getDeletable();
    void setDeletable(Boolean deletable);
}
