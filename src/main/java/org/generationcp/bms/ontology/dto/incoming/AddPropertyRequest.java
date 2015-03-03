package org.generationcp.bms.ontology.dto.incoming;

import java.util.ArrayList;
import java.util.List;

public class AddPropertyRequest {

    private String name;
    private String description;
    private String cropOntologyId;
    private List<String> classes;

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

    public String getCropOntologyId() {
        return cropOntologyId;
    }

    public void setCropOntologyId(String cropOntologyId) {
        this.cropOntologyId = cropOntologyId;
    }

    public List<String> getClasses() {
        if(classes == null) classes = new ArrayList<>();
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        return "Property [" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cropOntologyId='" + cropOntologyId + '\'' +
                ", classes=" + classes +
                ']';
    }

    public boolean validate(){
        if(this.getName().isEmpty() || this.getDescription().isEmpty() || this.getClasses().size() == 0){
            return false;
        }
        return true;
    }
}
