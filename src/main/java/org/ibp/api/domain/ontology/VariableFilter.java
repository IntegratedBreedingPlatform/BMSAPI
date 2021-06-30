package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

public class VariableFilter {

	private String programUuid;
	private boolean fetchAll;
	private boolean favoritesOnly;

	private final List<Integer> methodIds = new ArrayList<>();
	private final List<Integer> propertyIds = new ArrayList<>();
	private final List<Integer> scaleIds = new ArrayList<>();
	private final List<Integer> variableIds = new ArrayList<>();
	private final List<Integer> excludedVariableIds = new ArrayList<>();
	private final List<Integer> dataTypesIds = new ArrayList<>();
	private final List<Integer> variableTypeIds = new ArrayList<>();
	private final List<String> propertyClasses = new ArrayList<>();
	private final List<String> names = new ArrayList<>();

	public String getProgramUuid() {
		return programUuid;
	}

	public void setProgramUuid(final String programUuid) {
		this.programUuid = programUuid;
	}

	public boolean isFetchAll() {
		return fetchAll;
	}

	public void setFetchAll(final boolean fetchAll) {
		this.fetchAll = fetchAll;
	}

	public boolean isFavoritesOnly() {
		return favoritesOnly;
	}

	public void setFavoritesOnly(final boolean favoritesOnly) {
		this.favoritesOnly = favoritesOnly;
	}

	public List<Integer> getMethodIds() {
		return methodIds;
	}

	public void addMethodId(final Integer id) {
		this.methodIds.add(id);
	}

	public List<Integer> getPropertyIds() {
		return propertyIds;
	}

	public void addPropertyId(final Integer id) {
		this.propertyIds.add(id);
	}

	public List<Integer> getScaleIds() {
		return scaleIds;
	}

	public void addScaleId(final Integer id) {
		this.scaleIds.add(id);
	}

	public List<Integer> getVariableIds() {
		return variableIds;
	}

	public void addVariableId(final Integer id) {
		this.variableIds.add(id);
	}

	public List<Integer> getExcludedVariableIds() {
		return excludedVariableIds;
	}

	public void addExcludedVariableId(final Integer id) {
		this.excludedVariableIds.add(id);
	}

	public List<Integer> getDataTypes() {
		return dataTypesIds;
	}

	public void addDataType(final Integer dataType) {
		this.dataTypesIds.add(dataType);
	}

	public List<Integer> getVariableTypes() {
		return variableTypeIds;
	}

	public void addVariableType(final Integer variableType) {
		this.variableTypeIds.add(variableType);
	}

	public List<String> getPropertyClasses() {
		return propertyClasses;
	}

	public void addPropertyClass(final String className) {
		this.propertyClasses.add(className);
	}

	public void addName(final String name) {
		this.names.add(name);
	}

	public void addVariableIds(final List<Integer> variableIds) {
		this.variableIds.addAll(variableIds);
	}

	public List<String> getNames() {
		return names;
	}

	@Override public String toString() {
		return "VariableFilter{" +
				"programUuid='" + programUuid + '\'' +
				", fetchAll=" + fetchAll +
				", favoritesOnly=" + favoritesOnly +
				", methodIds=" + methodIds +
				", propertyIds=" + propertyIds +
				", scaleIds=" + scaleIds +
				", variableIds=" + variableIds +
				", excludedVariableIds=" + excludedVariableIds +
				", dataTypesIds=" + dataTypesIds +
				", variableTypeIds=" + variableTypeIds +
				", propertyClasses=" + propertyClasses +
			", names=" + names +
			'}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final VariableFilter that = (VariableFilter) o;

		if (fetchAll != that.fetchAll) {
			return false;
		}
		if (favoritesOnly != that.favoritesOnly) {
			return false;
		}
		if (programUuid != null ? !programUuid.equals(that.programUuid) : that.programUuid != null) {
			return false;
		}
		if (methodIds != null ? !methodIds.equals(that.methodIds) : that.methodIds != null) {
			return false;
		}
		if (propertyIds != null ? !propertyIds.equals(that.propertyIds) : that.propertyIds != null) {
			return false;
		}
		if (scaleIds != null ? !scaleIds.equals(that.scaleIds) : that.scaleIds != null) {
			return false;
		}
		if (variableIds != null ? !variableIds.equals(that.variableIds) : that.variableIds != null) {
			return false;
		}
		if (excludedVariableIds != null ? !excludedVariableIds.equals(that.excludedVariableIds) : that.excludedVariableIds != null) {
			return false;
		}
		if (dataTypesIds != null ? !dataTypesIds.equals(that.dataTypesIds) : that.dataTypesIds != null) {
			return false;
		}
		if (variableTypeIds != null ? !variableTypeIds.equals(that.variableTypeIds) : that.variableTypeIds != null) {
			return false;
		}
		if (names != null ? !names.equals(that.names) : that.names != null) {
			return false;
		}

		return !(propertyClasses != null ? !propertyClasses.equals(that.propertyClasses) : that.propertyClasses != null);

	}

	@Override public int hashCode() {
		int result = programUuid != null ? programUuid.hashCode() : 0;
		result = 31 * result + (fetchAll ? 1 : 0);
		result = 31 * result + (favoritesOnly ? 1 : 0);
		result = 31 * result + (methodIds != null ? methodIds.hashCode() : 0);
		result = 31 * result + (propertyIds != null ? propertyIds.hashCode() : 0);
		result = 31 * result + (scaleIds != null ? scaleIds.hashCode() : 0);
		result = 31 * result + (variableIds != null ? variableIds.hashCode() : 0);
		result = 31 * result + (excludedVariableIds != null ? excludedVariableIds.hashCode() : 0);
		result = 31 * result + (dataTypesIds != null ? dataTypesIds.hashCode() : 0);
		result = 31 * result + (variableTypeIds != null ? variableTypeIds.hashCode() : 0);
		result = 31 * result + (propertyClasses != null ? propertyClasses.hashCode() : 0);
		result = 31 * result + (names != null ? names.hashCode() : 0);

		return result;
	}
}
