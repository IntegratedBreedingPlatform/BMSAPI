package org.ibp.builders;

import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;

import java.util.List;

public class PropertyBuilder {

    public PropertyBuilder(){

    }

    public Property build(int id, String name, String description, String cropOntologyId, List<String> classes) {

        Property property = new Property();
        property.setId(id);
        property.setName(name);
        property.setDefinition(description);
        property.setCropOntologyId(cropOntologyId);

        for(String c : classes){
            property.addClass(c);
        }
        return property;
    }
}
