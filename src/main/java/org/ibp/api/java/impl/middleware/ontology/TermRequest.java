
package org.ibp.api.java.impl.middleware.ontology;

public class TermRequest {

	private String id;
	private String termName;
	private Integer cvId;

	public TermRequest(String id, String name, Integer cvId) {
		this.id = id;
		this.termName = name;
		this.cvId = cvId;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTermName() {
		return this.termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}

	public Integer getCvId() {
		return this.cvId;
	}

	public void setCvId(Integer cvId) {
		this.cvId = cvId;
	}

}
