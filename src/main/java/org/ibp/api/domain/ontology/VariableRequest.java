package org.ibp.api.domain.ontology;

public class VariableRequest extends UpdateVariableRequest {

	private String id;
	private String programUuid;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProgramUuid() {
		return programUuid;
	}

	public void setProgramUuid(String programUuid) {
		this.programUuid = programUuid;
	}

	@Override
	public String toString() {
		return "VariableRequest{" +
				"id='" + id + '\'' +
				", programUuid='" + programUuid + '\'' +
				"} " + super.toString();
	}
}
