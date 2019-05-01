
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Observation {

	private Integer uniqueIdentifier;

	private Integer germplasmId;

	private String germplasmDesignation;

	private String entryNumber;

	private String entryType;

	private String plotNumber;

	private String obsUnitId;

	private String blockNumber;

	private String replicationNumber;

	private String environmentNumber;

	private String entryCode;

	private List<Measurement> measurements;

	private String rowNumber;
	private String columnNumber;

	private List<Pair<String, String>> additionalGermplasmDescriptors = new ArrayList<>();

	@JsonIgnore
	private final Map<MeasurementIdentifier, Measurement> measurementsMap = new HashMap<>();

	public Observation() {

		this.updateMeasurementMap();
	}

	/**
	 * @return the uniqueIdentifier
	 */
	public Integer getUniqueIdentifier() {
		return this.uniqueIdentifier;
	}

	/**
	 * @param uniqueIdentifier the uniqueIdentifier to set
	 */
	public void setUniqueIdentifier(final Integer uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	/**
	 * @return the germplasmId
	 */
	public Integer getGermplasmId() {
		return this.germplasmId;
	}

	/**
	 * @param germplasmId the germplasmId to set
	 */
	public void setGermplasmId(final Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	/**
	 * @return the germplasmDesignation
	 */
	public String getGermplasmDesignation() {
		return this.germplasmDesignation;
	}

	/**
	 * @param germplasmDesignation the germplasmDesignation to set
	 */
	public void setGermplasmDesignation(final String germplasmDesignation) {
		this.germplasmDesignation = germplasmDesignation;
	}

	/**
	 * @return the entryNumber
	 */
	public String getEntryNumber() {
		return this.entryNumber;
	}

	/**
	 * @param entryNumber the enrtyNumber to set
	 */
	public void setEntryNumber(final String entryNumber) {
		this.entryNumber = entryNumber;
	}

	/**
	 * @return the entryType
	 */
	public String getEntryType() {
		return this.entryType;
	}

	/**
	 * @param entryType the entryType to set
	 */
	public void setEntryType(final String entryType) {
		this.entryType = entryType;
	}

	/**
	 * @return the plotNumber
	 */
	public String getPlotNumber() {
		return this.plotNumber;
	}

	/**
	 * @param plotNumber the plotNumber to set
	 */
	public void setPlotNumber(final String plotNumber) {
		this.plotNumber = plotNumber;
	}

	public String getBlockNumber() {
		return this.blockNumber;
	}

	public void setBlockNumber(final String blockNumber) {
		this.blockNumber = blockNumber;
	}

	public List<Pair<String, String>> getAdditionalGermplasmDescriptors() {
		return this.additionalGermplasmDescriptors;
	}

	public void setAdditionalGermplasmDescriptors(final List<Pair<String, String>> additionalGermplasmDescriptors) {
		this.additionalGermplasmDescriptors = additionalGermplasmDescriptors;
	}

	/**
	 * @return the replicationNumber
	 */
	public String getReplicationNumber() {
		return this.replicationNumber;
	}

	/**
	 * @param replicationNumber the replicationNumber to set
	 */
	public void setReplicationNumber(final String replicationNumber) {
		this.replicationNumber = replicationNumber;
	}

	/**
	 * @return the environmentNumber
	 */
	public String getEnvironmentNumber() {
		return this.environmentNumber;
	}

	/**
	 * @param environmentNumber the environmentNumber to set
	 */
	public void setEnvironmentNumber(final String environmentNumber) {
		this.environmentNumber = environmentNumber;
	}

	public String getEntryCode() {
		return this.entryCode;
	}

	public void setEntryCode(final String entryCode) {
		this.entryCode = entryCode;
	}

	/**
	 * @return the measurements
	 */
	public List<Measurement> getMeasurements() {
		return this.measurements;
	}

	public Measurement getMeasurement(final MeasurementIdentifier measurementIdentifier) {
		return this.measurementsMap.get(measurementIdentifier);
	}

	/**
	 * @param measurements the measurements to set
	 */
	public void setMeasurements(final List<Measurement> measurements) {
		this.measurements = measurements;
		this.updateMeasurementMap();
	}

	private void updateMeasurementMap() {
		if (this.measurements != null) {
			this.measurementsMap.clear();
			for (final Measurement measurement : this.measurements) {
				this.measurementsMap.put(measurement.getMeasurementIdentifier(), measurement);
			}
		}
	}

	public String getRowNumber() {
		return this.rowNumber;
	}

	public void setRowNumber(final String rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getColumnNumber() {
		return this.columnNumber;
	}

	public void setColumnNumber(final String columnNumber) {
		this.columnNumber = columnNumber;
	}

	public Map<MeasurementIdentifier, Measurement> getMeasurementsMap() {
		return this.measurementsMap;
	}

	public String getObsUnitId() {
		return this.obsUnitId;
	}

	public void setObsUnitId(final String obsUnitId) {
		this.obsUnitId = obsUnitId;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Observation)) {
			return false;
		}
		final Observation castOther = (Observation) other;
		return new EqualsBuilder().append(this.uniqueIdentifier, castOther.uniqueIdentifier).append(this.germplasmId, castOther.germplasmId)
				.append(this.germplasmDesignation, castOther.germplasmDesignation).append(this.entryNumber, castOther.entryNumber)
				.append(this.entryType, castOther.entryType).append(this.plotNumber, castOther.plotNumber)
				.append(this.replicationNumber, castOther.replicationNumber).append(this.environmentNumber, castOther.environmentNumber)
				.append(this.entryCode, castOther.entryCode).append(this.measurements, castOther.measurements).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.uniqueIdentifier).append(this.germplasmId).append(this.germplasmDesignation)
				.append(this.entryNumber).append(this.entryType).append(this.plotNumber).append(this.replicationNumber)
				.append(this.environmentNumber).append(this.entryCode).append(this.measurements).toHashCode();
	}

}
