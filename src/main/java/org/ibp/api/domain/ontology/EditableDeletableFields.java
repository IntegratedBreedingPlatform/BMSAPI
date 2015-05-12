package org.ibp.api.domain.ontology;

import java.util.List;

//TODO delete me once Scale/Propery/Variable are moved over to use MetadataDetails for editable/deletable fileds.
public interface EditableDeletableFields {

	List<String> getEditableFields();

	void setEditableFields(List<String> editableFields);

	Boolean getDeletable();

	void setDeletable(Boolean deletable);
}
