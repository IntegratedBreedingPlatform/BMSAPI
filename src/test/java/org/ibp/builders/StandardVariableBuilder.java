package org.ibp.builders;


import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.Term;

public class StandardVariableBuilder {

    private StandardVariable standardVariable;

    public StandardVariableBuilder() {
        standardVariable = new StandardVariable();
    }

    public StandardVariableBuilder id(int id) {
        standardVariable.setId(id);
        return this;
    }

    public StandardVariableBuilder name(String name) {
        standardVariable.setName(name);
        return this;
    }

    public StandardVariableBuilder description(String description) {
        standardVariable.setDescription(description);
        return this;
    }

    public StandardVariableBuilder cropOntologyId(String cropOntologyId) {
        standardVariable.setCropOntologyId(cropOntologyId);
        return this;
    }

    public StandardVariableBuilder setScale(int id, String name, String description) {
        standardVariable.setScale(new Term(id, name, description));
        return this;
    }

    public StandardVariableBuilder setProperty(int id, String name, String description) {
        standardVariable.setProperty(new Term(id, name, description));
        return this;
    }

    public StandardVariableBuilder setMethod(int id, String name, String description) {
        standardVariable.setMethod(new Term(id, name, description));
        return this;
    }

    public StandardVariableBuilder setDataType(int id, String name, String description) {
        standardVariable.setDataType(new Term(id, name, description));
        return this;
    }

    public StandardVariableBuilder setStoredIn(int id, String name, String description) {
        standardVariable.setStoredIn(new Term(id, name, description));
        return this;
    }

    public StandardVariableBuilder setIsA(int id, String name, String description) {
        standardVariable.setIsA(new Term(id, name, description));
        return this;
    }

    public StandardVariableBuilder setVariableConstraints(Double minValue, Double maxValue){
        standardVariable.setConstraints(new VariableConstraints(minValue, maxValue));
        return this;
    }

    public StandardVariable build(){
        return standardVariable;
    }
}
