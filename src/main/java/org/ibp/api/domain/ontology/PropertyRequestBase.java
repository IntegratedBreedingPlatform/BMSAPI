package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

public class PropertyRequestBase {

  private String name;
  private String description;
  private String cropOntologyId;
  private List<String> classes;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCropOntologyId() {
    return this.cropOntologyId;
  }

  public void setCropOntologyId(String cropOntologyId) {
    this.cropOntologyId = cropOntologyId;
  }

  public List<String> getClasses() {
    if (this.classes == null) {
      this.classes = new ArrayList<>();
    }
    return this.classes;
  }

  public void setClasses(List<String> classes) {
    this.classes = classes;
  }

  @Override
  public String toString() {
    return "PropertyRequestBase{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", cropOntologyId='" + cropOntologyId + '\'' +
            ", classes=" + classes +
            '}';
  }
}
