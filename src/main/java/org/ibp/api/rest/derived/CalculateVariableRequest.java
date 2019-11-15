package org.ibp.api.rest.derived;

import java.util.List;
import java.util.Map;

public class CalculateVariableRequest {
	private Integer variableId;
	private List<Integer> geoLocationIds;

	// Contains input variable id and dataset id from which input variable data will be read from.
	// This is to ensure that even if the input variable has multiple occurrences in study, the data will only
	// come from the dataset specified in this map.
	private Map<Integer, Integer> inputVariableDatasetMap;

	private boolean overwriteExistingData;

	public Integer getVariableId() {
		return this.variableId;
	}

	public void setVariableId(final Integer variableId) {
		this.variableId = variableId;
	}

	public List<Integer> getGeoLocationIds() {
		return this.geoLocationIds;
	}

	public void setGeoLocationIds(final List<Integer> geoLocationIds) {
		this.geoLocationIds = geoLocationIds;
	}

	public boolean isOverwriteExistingData() {
		return overwriteExistingData;
	}

	public void setOverwriteExistingData(final boolean overwriteExistingData) {
		this.overwriteExistingData = overwriteExistingData;
	}

	public Map<Integer, Integer> getInputVariableDatasetMap() {
		return this.inputVariableDatasetMap;
	}

	public void setInputVariableDatasetMap(final Map<Integer, Integer> inputVariableDatasetMap) {
		this.inputVariableDatasetMap = inputVariableDatasetMap;
	}

}
