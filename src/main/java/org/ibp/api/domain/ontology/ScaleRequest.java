package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScaleRequest {

    @JsonIgnore
    private Integer id;

    private String name;
    private String description;
    private Integer dataTypeId;
    private ValidValues validValues;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(Integer dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public ValidValues getValidValues() {
        return validValues;
    }

    public void setValidValues(ValidValues validValues) {
        this.validValues = validValues;
    }
}
