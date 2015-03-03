package org.generationcp.bms.ontology.builders;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.Property;

import java.util.List;

public class PropertyBuilder {

    private Term term;

    public PropertyBuilder(){
        term = new Term();
    }

    public Property build(int id, String name, String description, String cropOntologyId, List<Term> classes) {
        term.setId(id);
        term.setName(name);
        term.setDefinition(description);
        Property property = new Property(term);
        property.setCropOntologyId(cropOntologyId);
        property.setClasses(classes);
        return property;
    }
}
