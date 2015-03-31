package org.generationcp.bms.ontology.dto;

public class MethodSummary extends MethodRequest {
    
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Method [id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + "]";
    }
}