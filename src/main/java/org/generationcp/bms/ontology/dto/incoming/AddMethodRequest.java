package org.generationcp.bms.ontology.dto.incoming;

public class AddMethodRequest {

    private String name;
    private String description;

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
        return "Method [name=" + this.getName() + ", description=" + this.getDescription() + "]";
    }
}
