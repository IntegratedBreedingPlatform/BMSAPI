package org.ibp.api.rest.derived;

import java.util.List;

public class CalculateVariableRequest {
	private Integer variableId;
	private List<Integer> geoLocationIds;

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

}
