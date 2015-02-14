package org.generationcp.bms.ontology;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.Scale;

public class ScaleBuilder {

    private Term term;

    public ScaleBuilder(){
        term = new Term();
    }

    public ScaleBuilder id(int id) {
        term.setId(id);
        return this;
    }

    public ScaleBuilder name(String name) {
        term.setName(name);
        return this;
    }

    public ScaleBuilder definition(String definition) {
        term.setDefinition(definition);
        return this;
    }

    public Scale build(){
        return new Scale(term);
    }
}
