package org.generationcp.bms.ontology.builders;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.Property;

public class PropertyBuilder {

    private Term term;

    public PropertyBuilder(){
        term = new Term();
    }

    public Property build(int id, String name, String description) {
        term.setId(id);
        term.setName(name);
        term.setDefinition(description);
        return new Property(term);
    }
}
