package org.generationcp.bms.domain;

public class TermSummary {

	private Integer id;
	
	private String name;

	private String definition;

	public TermSummary() { }
	
	public TermSummary(Integer id, String name, String definition) {
		this.id = id;
		this.name = name;
		this.definition = definition;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDefinition() {
		return definition;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	@Override
	public int hashCode() {
		return getId();
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof TermSummary)) {
			return false;
		}
		
		TermSummary other = (TermSummary) obj;
		return getId().equals(other.getId());
	}
	
	@Override
	public String toString() {
		return "TermSummary [termId=" + id + ", name=" + name
				+ ", definition=" + definition + "]";
	}
	
}

