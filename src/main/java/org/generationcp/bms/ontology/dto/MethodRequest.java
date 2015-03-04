package org.generationcp.bms.ontology.dto;

public class MethodRequest {

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

    public boolean validate(){
        return !(this.getName().isEmpty() || this.getDescription().isEmpty());
    }
}
