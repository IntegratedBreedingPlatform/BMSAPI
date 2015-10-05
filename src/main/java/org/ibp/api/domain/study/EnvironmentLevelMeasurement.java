
package org.ibp.api.domain.study;

import javax.validation.constraints.NotNull;

public class EnvironmentLevelMeasurement {

	@NotNull
	private Integer variableId;

	@NotNull
	private String variableValue;

	public Integer getVariableId() {
		return this.variableId;
	}

	public void setVariableId(final Integer variableId) {
		this.variableId = variableId;
	}

	public String getVariableValue() {
		return this.variableValue;
	}

	public void setVariableValue(final String variableValue) {
		this.variableValue = variableValue;
	}

}
