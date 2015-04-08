package org.ibp.api.domain.ontology;

public class TermRequest {

    private Integer id;
    private String termName;
    private Integer cvId;

    public TermRequest(Integer id, String name, Integer cvId) {
        this.id = id;
        this.termName = name;
        this.cvId = cvId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public Integer getCvId() {
        return cvId;
    }

    public void setCvId(Integer cvId) {
        this.cvId = cvId;
    }


}
