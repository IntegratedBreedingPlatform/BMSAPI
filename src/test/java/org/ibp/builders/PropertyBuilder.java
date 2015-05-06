package org.ibp.builders;

import org.generationcp.middleware.domain.oms.OntologyProperty;

import java.util.List;

public class PropertyBuilder {

	public PropertyBuilder() {

	}

	public OntologyProperty build(int id, String name, String description, String cropOntologyId,
			List<String> classes) {

		OntologyProperty property = new OntologyProperty();
		property.setId(id);
		property.setName(name);
		property.setDefinition(description);
		property.setCropOntologyId(cropOntologyId);

		for (String c : classes) {
			property.addClass(c);
		}
		return property;
	}
}
