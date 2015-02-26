package org.generationcp.bms.ontology.builders;

import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;

public class ScaleBuilder {

    private Term term;

    public ScaleBuilder(){
        term = new Term();
    }

    public Scale build(int id, String name, String description)
    {
        term.setId(id);
        term.setName(name);
        term.setDefinition(description);
        return new Scale(term);
    }
}
