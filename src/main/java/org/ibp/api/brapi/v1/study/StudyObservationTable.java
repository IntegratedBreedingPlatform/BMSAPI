package org.ibp.api.brapi.v1.study;

import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"studyDbId", "headerRow", "observationVariableDbIds", "observationVariableNames", "data"})
public class StudyObservationTable {

	private Integer studyDbId;

	private List<Integer> observationVariableDbIds;

	private List<String> observationVariableNames;

	private List<String> headerRow;

	private List<List<String>> data;

	public Integer getStudyDbId() {
		return studyDbId;
	}

	public StudyObservationTable setStudyDbId(final Integer studyDbId) {
		this.studyDbId = studyDbId;
		return this;
	}

	public List<Integer> getObservationVariableDbIds() {
		return observationVariableDbIds;
	}

	public void setObservationVariableDbIds(List<Integer> observationVariableDbIds) {
		this.observationVariableDbIds = observationVariableDbIds;
	}

	public List<String> getObservationVariableNames() {
		return observationVariableNames;
	}

	public void setObservationVariableNames(List<String> observationVariableNames) {
		this.observationVariableNames = observationVariableNames;
	}

	public List<String> getHeaderRow() {
		return headerRow;
	}

	public void setHeaderRow(List<String> headerRow) {
		this.headerRow = headerRow;
	}

	public List<List<String>> getData() {
		return data;
	}

	public StudyObservationTable setData(final List<List<String>> data) {
		this.data = data;
		return this;
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
