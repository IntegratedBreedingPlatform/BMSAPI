package org.ibp.api.rest.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AutoProperty
public class ObservationUnitRow {

	private Integer observationUnitId;

	private Integer gid;

	private String designation;

	private Integer entryNumber;

	private Integer trialInstance;

	private String action;

	private String samplesCount;

	private String stockId;

	private Map<String, ObservationUnitData> variables;

	// Contains variables added in Environment Details and Environment Conditions
	private Map<String, ObservationUnitData> environmentVariables;

	public ObservationUnitRow() {

	}

	public Integer getObservationUnitId() {
		return this.observationUnitId;
	}

	public void setObservationUnitId(final Integer observationUnitId) {
		this.observationUnitId = observationUnitId;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(final String designation) {
		this.designation = designation;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(final String action) {
		this.action = action;
	}

	public Map<String, ObservationUnitData> getVariables() {
		return this.variables;
	}

	public void setVariables(final Map<String, ObservationUnitData> variables) {
		this.variables = variables;
	}

	public Map<String, ObservationUnitData> getEnvironmentVariables() {
		return this.environmentVariables;
	}

	public void setEnvironmentVariables(final Map<String, ObservationUnitData> environmentVariables) {
		this.environmentVariables = environmentVariables;
	}

	public String getSamplesCount() {
		return samplesCount;
	}

	public void setSamplesCount(final String samplesCount) {
		this.samplesCount = samplesCount;
	}

	public Integer getEntryNumber() {
		return entryNumber;
	}

	public void setEntryNumber(final Integer entryNumber) {
		this.entryNumber = entryNumber;
	}

	public Integer getTrialInstance() {
		return trialInstance;
	}

	public void setTrialInstance(final Integer trialInstance) {
		this.trialInstance = trialInstance;
	}

	public String getStockId() {
		return this.stockId;
	}

	public void setStockId(final String stockId) {
		this.stockId = stockId;
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}
}