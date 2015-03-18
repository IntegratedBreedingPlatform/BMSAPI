package org.generationcp.bms.ontology.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidValues{

    private String minValue;
    private String maxValue;
    private Map<String, String> categories;

    public ValidValues() {
    }

    public ValidValues(String minValue, String maxValue, Map<String, String> categories) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.categories = categories;
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

    public Map<String, String> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, String> categories) {
        this.categories = categories;
    }
}