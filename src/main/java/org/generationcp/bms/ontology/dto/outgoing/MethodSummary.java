package org.generationcp.bms.ontology.dto.outgoing;

public class MethodSummary {
    
    private Integer id;
    private String name;
    private String description;

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

    public String toString() {
        return "Method [id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + "]";
    }
}