
package org.ibp.api.brapi.v1.trial;

import java.util.ArrayList;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"studyDbId", "headerRow", "observationVariableDbIds", "observationVariableNames", "data"})
public class TrialObservationTable {

	private Integer trialDBId;

	private List<Integer> observationVariableDbIds = new ArrayList<>();

	private List<String> observationVariableNames = new ArrayList<>();

	private List<String> headerRow = new ArrayList<>();

	private List<List<String>> data = new ArrayList<>();

	public Integer getTrialDbId() {
		return this.trialDBId;
	}

	public TrialObservationTable setTrialDbId(final Integer studyDbId) {
		this.trialDBId = studyDbId;
		return this;
	}

	public List<Integer> getObservationVariableDbIds() {
		return this.observationVariableDbIds;
	}

	public void setObservationVariableDbIds(final List<Integer> observationVariableDbIds) {
		this.observationVariableDbIds = observationVariableDbIds;
	}

	public List<String> getObservationVariableNames() {
		return this.observationVariableNames;
	}

	public void setObservationVariableNames(final List<String> observationVariableNames) {
		this.observationVariableNames = observationVariableNames;
	}

	public List<String> getHeaderRow() {
		return this.headerRow;
	}

	public void setHeaderRow(final List<String> headerRow) {
		this.headerRow = headerRow;
	}

	public List<List<String>> getData() {
		return this.data;
	}

	public TrialObservationTable setData(final List<List<String>> data) {
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

}
