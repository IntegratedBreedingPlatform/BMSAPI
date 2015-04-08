package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class VariableRequest {

    @JsonIgnore
    private Integer id;
    @JsonIgnore
    private Integer programId;

    private String name;
    private String description;
    private Integer propertyId;
    private Integer methodId;
    private Integer scaleId;
    private List<Integer> variableTypeIds;
    private ExpectedRange expectedRange;

    public Integer getProgramId() {
        return programId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

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

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public Integer getMethodId() {
        return methodId;
    }

    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
    }

    public Integer getScaleId() {
        return scaleId;
    }

    public void setScaleId(Integer scaleId) {
        this.scaleId = scaleId;
    }

    public List<Integer> getVariableTypeIds() {
        return variableTypeIds;
    }

    public void setVariableTypeIds(List<Integer> variableTypeIds) {
        this.variableTypeIds = variableTypeIds;
    }

    public ExpectedRange getExpectedRange() {
        return expectedRange;
    }

    public void setExpectedRange(ExpectedRange expectedRange) {
        this.expectedRange = expectedRange;
    }
}
