package org.generationcp.bms.ontology.builders;

import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;

import java.util.List;

public class PropertyBuilder {

    public PropertyBuilder(){

    }

    public Property build(int id, String name, String description, String cropOntologyId, List<Term> classes) {

        Property property = new Property();
        property.setId(id);
        property.setName(name);
        property.setDefinition(description);
        property.setCropOntologyId(cropOntologyId);

        for(Term c : classes){
            property.addClass(c);
        }
        return property;
    }
}
