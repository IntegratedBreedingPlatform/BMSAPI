package org.generationcp.bms.ontology.dto;

public class TermRequest {

    private Integer id;
    private Integer cvId;

    public TermRequest(Integer id, Integer cvId) {
        this.id = id;
        this.cvId = cvId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCvId() {
        return cvId;
    }

    public void setCvId(Integer cvId) {
        this.cvId = cvId;
    }

}
