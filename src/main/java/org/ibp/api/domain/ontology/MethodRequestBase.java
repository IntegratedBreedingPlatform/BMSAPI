package org.ibp.api.domain.ontology;

public class MethodRequestBase {

  private String name;

  private String description;

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

  @Override public String toString() {
	return "MethodRequest{" +
			" name='" + name + '\'' +
			", description='" + description + '\'' +
			'}';
  }
}
