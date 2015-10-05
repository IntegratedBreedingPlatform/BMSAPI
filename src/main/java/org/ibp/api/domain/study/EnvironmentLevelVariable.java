
package org.ibp.api.domain.study;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class EnvironmentLevelVariable {

	@NotNull
	private Integer variableId;

	@NotBlank
	private String variableName;

	public Integer getVariableId() {
		return this.variableId;
	}

	public void setVariableId(final Integer variableId) {
		this.variableId = variableId;
	}

	public String getVariableName() {
		return this.variableName;
	}

	public void setVariableName(final String variableName) {
		this.variableName = variableName;
	}

}
