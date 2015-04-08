package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PropertyRequest {

    @JsonIgnore
    private Integer id;

    private String name;
    private String description;
    private String cropOntologyId;
    private List<String> classes;

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

    public String getCropOntologyId() {
        return cropOntologyId;
    }

    public void setCropOntologyId(String cropOntologyId) {
        this.cropOntologyId = cropOntologyId;
    }

    public List<String> getClasses() {
        if(classes == null) {
        	classes = new ArrayList<>();
        }
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public void setClassesFromSet(Set<String> classes){
        if(classes == null) {
            return;
        }

        this.classes = new ArrayList<>(classes);
    }

    @Override
    public String toString() {
        return "Property [id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cropOntologyId='" + cropOntologyId + '\'' +
                ", classes=" + classes +
                ']';
    }
}
