package org.generationcp.bms.ontology.dto;

import org.generationcp.middleware.domain.oms.VariableType;

import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;

public class VariableSummary {

    //TODO : Need to fetch alias and usage

    private Integer id;
    private String name;
    private String alias;
    private String description;
    private IdName propertySummary;
    private IdName methodSummary;
    private IdName scaleSummary;
    private List<IdName> variableTypes;
    private boolean favourite;
    private MetaData metadata;
    private ExpectedRange expectedRange;

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IdName getPropertySummary() {
        return propertySummary;
    }

    public void setPropertySummary(IdName propertySummary) {
        this.propertySummary = propertySummary;
    }

    public IdName getMethodSummary() {
        return methodSummary;
    }

    public void setMethodSummary(IdName methodSummary) {
        this.methodSummary = methodSummary;
    }

    public IdName getScaleSummary() {
        return scaleSummary;
    }

    public void setScaleSummary(IdName scaleSummary) {
        this.scaleSummary = scaleSummary;
    }

    public List<IdName> getVariableTypes() {
        return variableTypes;
    }

    public void setVariableTypes(Set<VariableType> variables) {
        if(this.variableTypes == null){
           this.variableTypes = new ArrayList<>();
        }

        this.variableTypes.clear();
        for(VariableType v : variables){
            this.variableTypes.add(new IdName(v.getId(), v.getName()));
        }
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public ExpectedRange getExpectedRange() {
        return expectedRange;
    }

    public void setExpectedMin(String min) {
        ensureExpectedRangeInitialized();
        this.expectedRange.setMin(min);
    }

    public void setExpectedMax(String max) {
        ensureExpectedRangeInitialized();
        this.expectedRange.setMax(max);
    }

    public void setModifiedData(Date modifiedDate) {
        ensureMetaDataInitialized();
        this.metadata.setDateLastModified(modifiedDate);
    }

    public void setCreatedDate(Date createdDate) {
        ensureMetaDataInitialized();
        this.metadata.setDateCreated(createdDate);
    }

    public void setObservations(Integer observations) {
        ensureMetaDataInitialized();
        this.metadata.setObservations(observations);
    }

    private void ensureMetaDataInitialized(){
        if(this.metadata == null){
            this.metadata = new MetaData();
        }
    }

    private void ensureExpectedRangeInitialized(){
        if(this.expectedRange == null){
            this.expectedRange = new ExpectedRange();
        }
    }

    @Override
    public String toString() {
        return "Variable [" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", description='" + description + '\'' +
                ", propertySummary=" + propertySummary +
                ", methodSummary=" + methodSummary +
                ", scaleSummary=" + scaleSummary +
                ", variableTypes=" + variableTypes +
                ", favourite=" + favourite +
                ", metadata=" + metadata +
                ", expectedRange=" + expectedRange +
                ']';
    }
}
