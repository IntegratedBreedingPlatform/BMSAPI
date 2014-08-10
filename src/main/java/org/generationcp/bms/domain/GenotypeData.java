package org.generationcp.bms.domain;

import java.util.List;

public class GenotypeData {

	private Integer runId;
	private String analysisMethod;
	private String encoding;
	private List<NameValuePair> data;

	public Integer getRunId() {
		return runId;
	}

	public void setRunId(Integer runId) {
		this.runId = runId;
	}

	public String getAnalysisMethod() {
		return analysisMethod;
	}

	public void setAnalysisMethod(String analysisMethod) {
		this.analysisMethod = analysisMethod;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public List<NameValuePair> getData() {
		return data;
	}

	public void setData(List<NameValuePair> data) {
		this.data = data;
	}

}
