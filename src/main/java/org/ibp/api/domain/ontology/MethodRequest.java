package org.ibp.api.domain.ontology;

public class MethodRequest extends MethodRequestBase {

  private String id;

  public String getId() {
	return id;
  }

  public void setId(String id) {
	this.id = id;
  }

  @Override public String toString() {
	  return "MethodRequest{" +
			  "id=" + this.getId() +
			  ", name='" + this.getName() + '\'' +
			  ", description='" + this.getDescription() + '\'' +
			  '}';
	}
}

