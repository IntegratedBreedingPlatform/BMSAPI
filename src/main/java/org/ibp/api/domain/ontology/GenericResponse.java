package org.ibp.api.domain.ontology;

public class GenericResponse {

    private int id;

    public GenericResponse(int id){
        this.setId(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
