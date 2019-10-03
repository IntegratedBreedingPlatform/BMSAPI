package org.ibp.api.rest.design;

import java.util.Map;

public class ExperimentDesignInput {

	private static final long serialVersionUID = -3081300491744812262L;
	private String noOfEnvironments;
	private String noOfEnvironmentsToAdd;
	private Integer designType;
	private String replicationsCount;
	private Boolean useLatenized;
	private String blockSize;
	private String rowsPerReplications;
	private String colsPerReplications;
	private Map treatmentFactors;
	private Map treatmentFactorsData;
	private String totalGermplasmListCount;
	private String nclatin;
	private String nrlatin;
	private String nblatin;
	private String replatinGroups;
	private boolean hasMeasurementData;
	private String startingPlotNo;
	private String startingEntryNo;
	private String fileName;
	private String numberOfBlocks;
	private String checkStartingPosition;
	private String checkSpacing;
	private String checkInsertionManner;
	private Integer replicationPercentage;

	/*
	 * 1 - single col 2 - single row 3 - adjacent
	 */
	private Integer replicationsArrangement;

	public String getNoOfEnvironments() {
		return this.noOfEnvironments;
	}

	public void setNoOfEnvironments(final String noOfEnvironments) {
		this.noOfEnvironments = noOfEnvironments;
	}

	public Integer getDesignType() {
		return this.designType;
	}

	public void setDesignType(final Integer designType) {
		this.designType = designType;
	}

	public String getReplicationsCount() {
		return this.replicationsCount;
	}

	public void setReplicationsCount(final String replicationsCount) {
		this.replicationsCount = replicationsCount;
	}

	public Boolean getUseLatenized() {
		return this.useLatenized;
	}

	public void setUseLatenized(final Boolean useLatenized) {
		this.useLatenized = useLatenized;
	}

	public String getBlockSize() {
		return this.blockSize;
	}

	public void setBlockSize(final String blockSize) {
		this.blockSize = blockSize;
	}

	public String getRowsPerReplications() {
		return this.rowsPerReplications;
	}

	public void setRowsPerReplications(final String rowsPerReplications) {
		this.rowsPerReplications = rowsPerReplications;
	}

	public String getColsPerReplications() {
		return this.colsPerReplications;
	}

	public void setColsPerReplications(final String colsPerReplications) {
		this.colsPerReplications = colsPerReplications;
	}

	public Map getTreatmentFactors() {
		return this.treatmentFactors;
	}

	public void setTreatmentFactors(final Map treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
	}

	public Map getTreatmentFactorsData() {
		return this.treatmentFactorsData;
	}

	public void setTreatmentFactorsData(final Map treatmentFactorsData) {
		this.treatmentFactorsData = treatmentFactorsData;
	}

	public String getTotalGermplasmListCount() {
		return this.totalGermplasmListCount;
	}

	public void setTotalGermplasmListCount(final String totalGermplasmListCount) {
		this.totalGermplasmListCount = totalGermplasmListCount;
	}

	public String getNclatin() {
		return this.nclatin;
	}

	public void setNclatin(final String nclatin) {
		this.nclatin = nclatin;
	}

	public String getNrlatin() {
		return this.nrlatin;
	}

	public void setNrlatin(final String nrlatin) {
		this.nrlatin = nrlatin;
	}

	public String getNblatin() {
		return this.nblatin;
	}

	public void setNblatin(final String nblatin) {
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

	public String getNoOfEnvironmentsToAdd() {
		return this.noOfEnvironmentsToAdd;
	}

	public void setNoOfEnvironmentsToAdd(final String noOfEnvironmentsToAdd) {
		this.noOfEnvironmentsToAdd = noOfEnvironmentsToAdd;
	}

	public boolean isHasMeasurementData() {
		return this.hasMeasurementData;
	}

	public void setHasMeasurementData(final boolean hasMeasurementData) {
		this.hasMeasurementData = hasMeasurementData;
	}

	public String getStartingPlotNo() {
		return this.startingPlotNo;
	}

	public void setStartingPlotNo(final String startingPlotNo) {
		this.startingPlotNo = startingPlotNo;
	}

	public String getStartingEntryNo() {
		return this.startingEntryNo;
	}

	public void setStartingEntryNo(final String startingEntryNo) {
		this.startingEntryNo = startingEntryNo;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public String getNumberOfBlocks() {
		return this.numberOfBlocks;
	}

	public void setNumberOfBlocks(final String numberOfBlocks) {
		this.numberOfBlocks = numberOfBlocks;
	}

	public String getCheckStartingPosition() {
		return this.checkStartingPosition;
	}

	public void setCheckStartingPosition(final String checkStartingPosition) {
		this.checkStartingPosition = checkStartingPosition;
	}

	public String getCheckSpacing() {
		return this.checkSpacing;
	}

	public void setCheckSpacing(final String checkSpacing) {
		this.checkSpacing = checkSpacing;
	}

	public String getCheckInsertionManner() {
		return this.checkInsertionManner;
	}

	public void setCheckInsertionManner(final String checkInsertionManner) {
		this.checkInsertionManner = checkInsertionManner;
	}

	public Integer getReplicationPercentage() {
		return this.replicationPercentage;
	}

	public void setReplicationPercentage(final Integer replicationPercentage) {
		this.replicationPercentage = replicationPercentage;
	}

}
