package org.ibp.api.domain.study;

import org.ibp.api.domain.dataset.DatasetVariable;
import org.pojomatic.Pojomatic;

import java.util.List;

public class StudyEntryDetailsImportRequest {

	private List<StudyEntryDetailsValueMap> data;

	private List<DatasetVariable> newVariables;

	public StudyEntryDetailsImportRequest() {
		super();
	}

	public StudyEntryDetailsImportRequest(final List<StudyEntryDetailsValueMap> data,
		final List<DatasetVariable> newVariables) {
		super();
		this.data = data;
		this.newVariables = newVariables;
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

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
