
package org.ibp.api.domain.study;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Observation {

	private Integer uniqueIdentifier;
	private Integer germplasmId;
	private String germplasmDesignation;
	private String enrtyNumber;
	private String entryType;
	private String plotNumber;
	private String replicationNumber;
	private String environmentNumber;
	private String seedSource;

	private List<Measurement> measurements;

	@JsonIgnore
	private final Map<MeasurementIdentifier, Measurement> measurementsMap = new HashMap<MeasurementIdentifier, Measurement>();

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
	public void setUniqueIdentifier(Integer uniqueIdentifier) {
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
	public void setGermplasmId(Integer germplasmId) {
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
	public void setGermplasmDesignation(String germplasmDesignation) {
		this.germplasmDesignation = germplasmDesignation;
	}

	/**
	 * @return the enrtyNumber
	 */
	public String getEnrtyNumber() {
		return this.enrtyNumber;
	}

	/**
	 * @param enrtyNumber the enrtyNumber to set
	 */
	public void setEnrtyNumber(String enrtyNumber) {
		this.enrtyNumber = enrtyNumber;
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
	public void setEntryType(String entryType) {
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
	public void setPlotNumber(String plotNumber) {
		this.plotNumber = plotNumber;
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
	public void setReplicationNumber(String replicationNumber) {
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
	public void setEnvironmentNumber(String environmentNumber) {
		this.environmentNumber = environmentNumber;
	}

	/**
	 * @return the seedSource
	 */
	public String getSeedSource() {
		return this.seedSource;
	}

	/**
	 * @param seedSource the seedSource to set
	 */
	public void setSeedSource(String seedSource) {
		this.seedSource = seedSource;
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

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Observation)) {
			return false;
		}
		Observation castOther = (Observation) other;
		return new EqualsBuilder().append(this.uniqueIdentifier, castOther.uniqueIdentifier)
				.append(this.germplasmId, castOther.germplasmId).append(this.germplasmDesignation, castOther.germplasmDesignation)
				.append(this.enrtyNumber, castOther.enrtyNumber).append(this.entryType, castOther.entryType)
				.append(this.plotNumber, castOther.plotNumber).append(this.replicationNumber, castOther.replicationNumber)
				.append(this.environmentNumber, castOther.environmentNumber).append(this.seedSource, castOther.seedSource)
				.append(this.measurements, castOther.measurements).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.uniqueIdentifier).append(this.germplasmId).append(this.germplasmDesignation)
				.append(this.enrtyNumber).append(this.entryType).append(this.plotNumber).append(this.replicationNumber)
				.append(this.environmentNumber).append(this.seedSource).append(this.measurements).toHashCode();
	}

}
