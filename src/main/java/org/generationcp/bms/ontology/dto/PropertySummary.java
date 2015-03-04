package org.generationcp.bms.ontology.dto;

public class PropertySummary extends PropertyRequest {

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String toString() {
        return "Property [" +
                "id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", cropOntologyId='" + this.getCropOntologyId() + '\'' +
                ", classes=" + this.getClasses() +
                ']';
    }
}
