package org.ibp.api.domain.study;

import org.ibp.api.domain.dataset.DatasetVariable;

import java.util.List;

public class StudyEntryDetailsImportRequest {

	private String programUuid;

	private List<StudyEntryDetailsValueMap> data;

	private List<DatasetVariable> newVariables;

	public StudyEntryDetailsImportRequest() {
		super();
	}

	public StudyEntryDetailsImportRequest(final String programUuid, final List<StudyEntryDetailsValueMap> data,
		final List<DatasetVariable> newVariables) {
		super();
		this.programUuid = programUuid;
		this.data = data;
		this.newVariables = newVariables;
	}

	public String getProgramUuid() {
		return this.programUuid;
	}

	public void setProgramUuid(final String programUuid) {
		this.programUuid = programUuid;
	}

	public List<StudyEntryDetailsValueMap> getData() {
		return this.data;
	}

	public void setData(final List<StudyEntryDetailsValueMap> data) {
		this.data = data;
	}

	public List<DatasetVariable> getNewVariables() {
		return this.newVariables;
	}

	public void setNewVariables(final List<DatasetVariable> newVariables) {
		this.newVariables = newVariables;
	}

}
