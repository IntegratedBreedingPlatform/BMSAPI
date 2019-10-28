package org.ibp.api.rest.design;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;
import java.util.Set;

@AutoProperty
public class ExperimentDesignInput {

	private Set<Integer> trialInstancesForDesignGeneration;
	private Integer designType;
	private Integer replicationsCount;
	private Boolean useLatenized;
	private Integer blockSize;
	private Integer rowsPerReplications;
	private Integer colsPerReplications;
	private Map treatmentFactorsData;
	private Integer nclatin;
	private Integer nrlatin;
	private Integer nblatin;
	private String replatinGroups;
	private Integer startingPlotNo;
	private String fileName;
	private Integer numberOfBlocks;
	private Integer checkStartingPosition;
	private Integer checkSpacing;
	private Integer checkInsertionManner;
	private Integer replicationPercentage;

	/*
	 * 1 - single col 2 - single row 3 - adjacent
	 */
	private Integer replicationsArrangement;

	public Set<Integer> getTrialInstancesForDesignGeneration() {
		return trialInstancesForDesignGeneration;
	}

	public void setTrialInstancesForDesignGeneration(final Set<Integer> trialInstancesForDesignGeneration) {
		this.trialInstancesForDesignGeneration = trialInstancesForDesignGeneration;
	}

	public Integer getDesignType() {
		return this.designType;
	}

	public void setDesignType(final Integer designType) {
		this.designType = designType;
	}

	public Integer getReplicationsCount() {
		return this.replicationsCount;
	}

	public void setReplicationsCount(final Integer replicationsCount) {
		this.replicationsCount = replicationsCount;
	}

	public Boolean getUseLatenized() {
		return this.useLatenized;
	}

	public void setUseLatenized(final Boolean useLatenized) {
		this.useLatenized = useLatenized;
	}

	public Integer getBlockSize() {
		return this.blockSize;
	}

	public void setBlockSize(final Integer blockSize) {
		this.blockSize = blockSize;
	}

	public Integer getRowsPerReplications() {
		return this.rowsPerReplications;
	}

	public void setRowsPerReplications(final Integer rowsPerReplications) {
		this.rowsPerReplications = rowsPerReplications;
	}

	public Integer getColsPerReplications() {
		return this.colsPerReplications;
	}

	public void setColsPerReplications(final Integer colsPerReplications) {
		this.colsPerReplications = colsPerReplications;
	}

	public Map getTreatmentFactorsData() {
		return this.treatmentFactorsData;
	}

	public void setTreatmentFactorsData(final Map treatmentFactorsData) {
		this.treatmentFactorsData = treatmentFactorsData;
	}

	public Integer getNclatin() {
		return this.nclatin;
	}

	public void setNclatin(final Integer nclatin) {
		this.nclatin = nclatin;
	}

	public Integer getNrlatin() {
		return this.nrlatin;
	}

	public void setNrlatin(final Integer nrlatin) {
		this.nrlatin = nrlatin;
	}

	public Integer getNblatin() {
		return this.nblatin;
	}

	public void setNblatin(final Integer nblatin) {
		this.nblatin = nblatin;
	}

	public String getReplatinGroups() {
		return this.replatinGroups;
	}

	public void setReplatinGroups(final String replatinGroups) {
		this.replatinGroups = replatinGroups;
	}

	public Integer getReplicationsArrangement() {
		return this.replicationsArrangement;
	}

	public void setReplicationsArrangement(final Integer replicationsArrangement) {
		this.replicationsArrangement = replicationsArrangement;
	}

	public Integer getStartingPlotNo() {
		return this.startingPlotNo;
	}

	public void setStartingPlotNo(final Integer startingPlotNo) {
		this.startingPlotNo = startingPlotNo;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public Integer getNumberOfBlocks() {
		return this.numberOfBlocks;
	}

	public void setNumberOfBlocks(final Integer numberOfBlocks) {
		this.numberOfBlocks = numberOfBlocks;
	}

	public Integer getCheckStartingPosition() {
		return this.checkStartingPosition;
	}

	public void setCheckStartingPosition(final Integer checkStartingPosition) {
		this.checkStartingPosition = checkStartingPosition;
	}

	public Integer getCheckSpacing() {
		return this.checkSpacing;
	}

	public void setCheckSpacing(final Integer checkSpacing) {
		this.checkSpacing = checkSpacing;
	}

	public Integer getCheckInsertionManner() {
		return this.checkInsertionManner;
	}

	public void setCheckInsertionManner(final Integer checkInsertionManner) {
		this.checkInsertionManner = checkInsertionManner;
	}

	public Integer getReplicationPercentage() {
		return this.replicationPercentage;
	}

	public void setReplicationPercentage(final Integer replicationPercentage) {
		this.replicationPercentage = replicationPercentage;
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
