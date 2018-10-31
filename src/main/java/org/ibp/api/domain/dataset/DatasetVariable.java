package org.ibp.api.domain.dataset;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class DatasetVariable {
	
	@NotNull
	private Integer variableTypeId;
	
	@NotNull
	private Integer variableId;
	
	@NotBlank
	private String studyAlias;
	
	public DatasetVariable() {
		super();
	}
	
	public DatasetVariable(Integer variableTypeId, Integer variableId, String studyAlias) {
		super();
		this.variableTypeId = variableTypeId;
		this.variableId = variableId;
		this.studyAlias = studyAlias;
	}

	public Integer getVariableTypeId() {
		return variableTypeId;
	}
	
	public void setVariableTypeId(Integer variableTypeId) {
		this.variableTypeId = variableTypeId;
	}
	
	
	public Integer getVariableId() {
		return variableId;
	}

	
	public void setVariableId(Integer variableId) {
		this.variableId = variableId;
	}

	public String getStudyAlias() {
		return studyAlias;
	}
	
	public void setStudyAlias(String studyAlias) {
		this.studyAlias = studyAlias;
	}

}
