package org.ibp.api.domain.ontology;

import java.util.List;

public class AddVariableRequest {
	private String name;
	private String description;
	private String propertyId;
	private String methodId;
	private String scaleId;
	private List<String> variableTypeIds;
	private ExpectedRange expectedRange;
	private boolean favourite;

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

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public String getMethodId() {
		return methodId;
	}

	public void setMethodId(String methodId) {
		this.methodId = methodId;
	}

	public String getScaleId() {
		return scaleId;
	}

	public void setScaleId(String scaleId) {
		this.scaleId = scaleId;
	}

	public List<String> getVariableTypeIds() {
		return variableTypeIds;
	}

	public void setVariableTypeIds(List<String> variableTypeIds) {
		this.variableTypeIds = variableTypeIds;
	}

	public ExpectedRange getExpectedRange() {
		return expectedRange;
	}

	public void setExpectedRange(ExpectedRange expectedRange) {
		this.expectedRange = expectedRange;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	@Override
	public String toString() {
		return "AddVariableRequest{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", propertyId='" + propertyId + '\'' +
				", methodId='" + methodId + '\'' +
				", scaleId='" + scaleId + '\'' +
				", variableTypeIds=" + variableTypeIds +
				", expectedRange=" + expectedRange +
				", favourite=" + favourite +
				'}';
	}
}

