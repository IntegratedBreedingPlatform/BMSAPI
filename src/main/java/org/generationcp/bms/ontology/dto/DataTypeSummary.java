package org.generationcp.bms.ontology.dto;


public class DataTypeSummary {

    private Integer id;
    private String name;

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

    public String toString() {
        return "DataTypeSummary[" + "id=" + id + ", name='" + name + '\'' + ']';
    }
}
