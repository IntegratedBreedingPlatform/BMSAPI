package org.generationcp.bms.ontology.dto;

public class GenericResponse {

    public GenericResponse(int id){
        this.setId(id);
    }
    
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
