package org.ibp.api.domain.ontology;

import java.util.List;

public class AddVariableRequest {
	private String programUuid;
	private String name;
	private String description;
	private Integer propertyId;
	private Integer methodId;
	private Integer scaleId;
	private List<Integer> variableTypeIds;
	private ExpectedRange expectedRange;

	public String getProgramUuid() {
		return programUuid;
	}

	public void setProgramUuid(String programUuid) {
		this.programUuid = programUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Integer propertyId) {
		this.propertyId = propertyId;
	}

	public Integer getMethodId() {
		return methodId;
	}

	public void setMethodId(Integer methodId) {
		this.methodId = methodId;
	}

	public Integer getScaleId() {
		return scaleId;
	}

	public void setScaleId(Integer scaleId) {
		this.scaleId = scaleId;
	}

	public List<Integer> getVariableTypeIds() {
		return variableTypeIds;
	}

	public void setVariableTypeIds(List<Integer> variableTypeIds) {
		this.variableTypeIds = variableTypeIds;
	}

	public ExpectedRange getExpectedRange() {
		return expectedRange;
	}

	public void setExpectedRange(ExpectedRange expectedRange) {
		this.expectedRange = expectedRange;
	}

	@Override
	public String toString() {
		return "AddVariableRequest{" +
				"programUuid='" + programUuid + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", propertyId=" + propertyId +
				", methodId=" + methodId +
				", scaleId=" + scaleId +
				", variableTypeIds=" + variableTypeIds +
				", expectedRange=" + expectedRange +
				'}';
	}
}

