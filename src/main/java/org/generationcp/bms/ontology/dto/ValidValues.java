package org.generationcp.bms.ontology.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidValues{

    private String minValue;
    private String maxValue;
    private List<NameDescription> categories;

    public ValidValues() {
    }

    public ValidValues(String minValue, String maxValue, Map<String, String> categories) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        mapCategories(categories);
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public List<NameDescription> getCategories() {
        return categories;
    }

    public void setCategoriesFromMap(Map<String, String> categories) {
        mapCategories(categories);
    }

    public void setCategories(List<NameDescription> categories) { this.categories = categories; }

    private void mapCategories(Map<String, String> categories){
        if(categories != null){
            this.categories = new ArrayList<>();
            for(String k : categories.keySet()){
                this.categories.add(new NameDescription(k, categories.get(k)));
            }
        }
    }
}