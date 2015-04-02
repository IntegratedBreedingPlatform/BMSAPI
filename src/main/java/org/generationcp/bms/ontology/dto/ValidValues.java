package org.generationcp.bms.ontology.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidValues{

    private String min;
    private String max;
    private List<NameDescription> categories;

    public ValidValues() {
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public List<NameDescription> getCategories() {
        return categories;
    }

    @JsonIgnore
    public void setCategoriesFromMap(Map<String, String> categories) {
        mapCategories(categories);
    }

    public void setCategories(List<NameDescription> categories) {
        this.categories = categories;
    }

    private void mapCategories(Map<String, String> categories){
        if(categories != null){
            this.categories = new ArrayList<>();
            for(String k : categories.keySet()){
                this.categories.add(new NameDescription(k, categories.get(k)));
            }
        }
    }
}
