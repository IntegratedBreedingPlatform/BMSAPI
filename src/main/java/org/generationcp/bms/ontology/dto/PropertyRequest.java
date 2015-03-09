package org.generationcp.bms.ontology.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.ArrayList;

public class PropertyRequest {

    private String name;
    private String description;
    private String cropOntologyId;
    private List<String> classes;

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

    public String getCropOntologyId() {
        return cropOntologyId;
    }

    public void setCropOntologyId(String cropOntologyId) {
        this.cropOntologyId = cropOntologyId;
    }

    public List<String> getClasses() {
        if(classes == null) classes = new ArrayList<>();
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        return "Property [" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cropOntologyId='" + cropOntologyId + '\'' +
                ", classes=" + classes +
                ']';
    }

    @JsonIgnore
    public boolean isValid(){
        return !(this.getName().isEmpty() || this.getDescription().isEmpty()) && this.classes.size() > 0;
    }
}
