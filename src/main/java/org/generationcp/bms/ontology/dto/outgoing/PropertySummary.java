package org.generationcp.bms.ontology.dto.outgoing;


import java.util.ArrayList;
import java.util.List;

public class PropertySummary {
    private Integer id;
    private String name;
    private String description;
    private String cropOntologyId;
    private List<String> classes = new ArrayList<>();

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
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public String toString() {
        return "Property [" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cropOntologyId='" + cropOntologyId + '\'' +
                ", classes=" + classes +
                ']';
    }
}
