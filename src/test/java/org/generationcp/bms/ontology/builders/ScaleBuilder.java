package org.generationcp.bms.ontology.builders;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.DataType;

import java.util.Map;

public class ScaleBuilder {

    private Term term;

    public ScaleBuilder(){
        term = new Term();
    }

    public Scale build(int id, String name, String description, DataType dataType, String minValue, String maxValue, Map<String, String> categories) {
        term.setId(id);
        term.setName(name);
        term.setDefinition(description);
        Scale scale = new Scale(term);
        scale.setDataType(dataType);
        scale.setMinValue(minValue);
        scale.setMaxValue(maxValue);
        if (categories !=null){
            for(String k : categories.keySet()){
                scale.addCategory(k, categories.get(k));
            }
        }
        return scale;
    }
}
