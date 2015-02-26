package org.generationcp.bms.ontology.dto.outgoing;

public class GenericAddResponse {

    public GenericAddResponse(int id){
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
