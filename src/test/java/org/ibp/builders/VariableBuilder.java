package org.ibp.builders;

import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.TermSummary;

public class VariableBuilder {

    public VariableBuilder() {
    }

    public OntologyVariableSummary build(Integer id, String name, String description, TermSummary methodSummary, TermSummary propertySummary, TermSummary scaleSummary){
        OntologyVariableSummary variableSummary = new OntologyVariableSummary(id, name, description);
        variableSummary.setMethodSummary(methodSummary);
        variableSummary.setPropertySummary(propertySummary);
        variableSummary.setScaleSummary(scaleSummary);
        return variableSummary;
    }
}
