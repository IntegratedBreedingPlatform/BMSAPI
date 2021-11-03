package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class LabelsInfoInput {

	private Integer studyId;
	private Integer datasetId;
	private Integer searchRequestId;
	private Integer listId;
	private String sortBy;

	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
	}

	public Integer getDatasetId() {
		return this.datasetId;
	}

	public void setDatasetId(final Integer datasetId) {
		this.datasetId = datasetId;
	}

	public Integer getSearchRequestId() {
		return this.searchRequestId;
	}

	public void setSearchRequestId(final Integer searchRequestId) {
		this.searchRequestId = searchRequestId;
	}

	public Integer getListId() {
		return this.listId;
	}

	public void setListId(final Integer listId) {
		this.listId = listId;
	}

	public String getSortBy() {
		return this.sortBy;
	}

	public void setSortBy(final String sortBy) {
		this.sortBy = sortBy;
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
