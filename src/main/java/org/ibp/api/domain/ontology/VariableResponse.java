package org.ibp.api.domain.ontology;

import org.generationcp.middleware.domain.oms.VariableType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class VariableResponse implements EditableDeletableFields{

    private String name;
    private String alias;
    private String description;
    private MethodSummary method;
    private PropertySummary property;
    private ScaleSummary scale;
    private List<IdName> variableTypes;
    private boolean favourite;
    private MetaData metadata;
    private ExpectedRange expectedRange;
    private List<String> editableFields;
    private Boolean deletable;

    public MethodSummary getMethod() {
        return method;
    }

    public void setMethod(MethodSummary method) {
        this.method = method;
    }

    public PropertySummary getProperty() {
        return property;
    }

    public void setProperty(PropertySummary property) {
        this.property = property;
    }

    public ScaleSummary getScale() {
        return scale;
    }

    public void setScale(ScaleSummary scale) {
        this.scale = scale;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
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

    @Override
    public List<String> getEditableFields() {
        return editableFields;
    }

    @Override
    public void setEditableFields(List<String> editableFields) {
        this.editableFields = editableFields;
    }

    @Override
    public Boolean getDeletable() {
        return deletable;
    }

    @Override
    public void setDeletable(Boolean deletable) {
        this.deletable = deletable;
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
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", description='" + description + '\'' +
                ", method=" + method +
                ", property=" + property +
                ", scale=" + scale +
                ", variableTypes=" + variableTypes +
                ", favourite=" + favourite +
                ", metadata=" + metadata +
                ", expectedRange=" + expectedRange +
                ", editableFields=" + editableFields +
                ", deletable=" + deletable +
                ']';
    }
}
