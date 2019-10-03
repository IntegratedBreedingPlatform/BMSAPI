
package org.ibp.api.domain.design;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BVDesignOutput implements Serializable {

	private static final long serialVersionUID = -1079217702697901056L;
	private int returnCode;
	private String[] bvHeaders;
	private List<BVDesignTrialInstance> trialInstances;

	public BVDesignOutput(int returnCode) {
		super();
		this.returnCode = returnCode;
	}

	public void setResults(final List<String[]> entries) {
		if (entries != null && !entries.isEmpty()) {
			this.trialInstances = new ArrayList<>();
			// 1st row is always the header row
			this.setBvHeaders(entries.get(0));
			Integer currentTrialInstance = 1;

			List<Map<String, String>> trialInstanceRows = new ArrayList<>();
			for (int i = 1; i < entries.size(); i++) {
				final String[] currentRow = entries.get(i);
				final Integer latestTrialInstance = Integer.valueOf(currentRow[0]);
				if (latestTrialInstance > currentTrialInstance) {
					this.trialInstances.add(new BVDesignTrialInstance(currentTrialInstance, trialInstanceRows));
					trialInstanceRows = new ArrayList<>();
					currentTrialInstance = latestTrialInstance;
				}
				final Map<String, String> rowValues = new HashMap<>();
				// Exclude the 1st column, which is the trial instance #
				for (int index = 1; index < this.bvHeaders.length; index++) {
					rowValues.put(this.bvHeaders[index], currentRow[index]);
				}
				trialInstanceRows.add(rowValues);
			}
			// add the last trial instance to list
			this.trialInstances.add(new BVDesignTrialInstance(currentTrialInstance, trialInstanceRows));
		}
	}

	public int getReturnCode() {
		return this.returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String[] getBvHeaders() {
		return this.bvHeaders;
	}

	public void setBvHeaders(String[] bvHeaders) {
		this.bvHeaders = bvHeaders;
	}

	public boolean isSuccess() {
		return (this.returnCode == 0);
	}

	public List<BVDesignTrialInstance> getTrialInstances() {
		return trialInstances;
	}

}
