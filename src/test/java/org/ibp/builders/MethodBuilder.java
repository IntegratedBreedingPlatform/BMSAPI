package org.ibp.builders;

import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Term;

public class MethodBuilder {

    private Term term;

    public MethodBuilder(){
        term = new Term();
    }

    public Method build(int id, String name, String description)
    {
        term.setId(id);
        term.setName(name);
        term.setDefinition(description);
        return new Method(term);
    }
}
