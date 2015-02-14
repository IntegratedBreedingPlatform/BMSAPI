package org.generationcp.bms.ontology;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Method;

public class Builder {

    private Term term;

    public Builder(){
        term = new Term();
    }

    public Builder id(int id) {
        term.setId(id);
        return this;
    }

    public Builder name(String name) {
        term.setName(name);
        return this;
    }

    public Builder definition(String definition) {
        term.setDefinition(definition);
        return this;
    }

    public Scale buildScale(){
        return new Scale(term);
    }

    public Method buildMethod(){
        return new Method(term);
    }
}
