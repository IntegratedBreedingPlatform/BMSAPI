package org.generationcp.bms.domain;

public class MarkerCount {
	
	private final int runId;
	private final String analysisMethod;
	private final long resultCount;
	
	public MarkerCount(int runId, String analysisMethod, long resultCount) {
		this.runId = runId;
		this.analysisMethod = analysisMethod;
		this.resultCount = resultCount;
	}

	public int getRunId() {
		return runId;
	}

	public String getAnalysisMethod() {
		return analysisMethod;
	}

	public long getResultCount() {
		return resultCount;
	}

}
