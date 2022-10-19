package org.ibp.api.domain.ontology;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.List;

@AutoProperty
public class VariableFilter {

	private String programUuid;
	private boolean fetchAll;
	private boolean favoritesOnly;
	private boolean showObsoletes = false;

	private final List<Integer> methodIds = new ArrayList<>();
	private final List<Integer> propertyIds = new ArrayList<>();
	private final List<Integer> scaleIds = new ArrayList<>();
	private final List<Integer> variableIds = new ArrayList<>();
	private final List<Integer> excludedVariableIds = new ArrayList<>();
	private final List<Integer> dataTypesIds = new ArrayList<>();
	private final List<Integer> variableTypeIds = new ArrayList<>();
	private final List<String> propertyClasses = new ArrayList<>();
	private final List<String> names = new ArrayList<>();
	private final List<Integer> datasetIds = new ArrayList<>();
	private final List<String> germplasmUUIDs = new ArrayList<>();
	private final List<Integer> lotIds = new ArrayList<>();

	public String getProgramUuid() {
		return this.programUuid;
	}

	public void setProgramUuid(final String programUuid) {
		this.programUuid = programUuid;
	}

	public boolean isFetchAll() {
		return this.fetchAll;
	}

	public void setFetchAll(final boolean fetchAll) {
		this.fetchAll = fetchAll;
	}

	public boolean isFavoritesOnly() {
		return this.favoritesOnly;
	}

	public void setFavoritesOnly(final boolean favoritesOnly) {
		this.favoritesOnly = favoritesOnly;
	}

	public List<Integer> getMethodIds() {
		return this.methodIds;
	}

	public void addMethodId(final Integer id) {
		this.methodIds.add(id);
	}

	public List<Integer> getPropertyIds() {
		return this.propertyIds;
	}

	public void addPropertyId(final Integer id) {
		this.propertyIds.add(id);
	}

	public List<Integer> getScaleIds() {
		return this.scaleIds;
	}

	public void addScaleId(final Integer id) {
		this.scaleIds.add(id);
	}

	public List<Integer> getVariableIds() {
		return this.variableIds;
	}

	public void addVariableId(final Integer id) {
		this.variableIds.add(id);
	}

	public List<Integer> getExcludedVariableIds() {
		return this.excludedVariableIds;
	}

	public void addExcludedVariableId(final Integer id) {
		this.excludedVariableIds.add(id);
	}

	public List<Integer> getDataTypes() {
		return this.dataTypesIds;
	}

	public void addDataType(final Integer dataType) {
		this.dataTypesIds.add(dataType);
	}

	public List<Integer> getVariableTypes() {
		return this.variableTypeIds;
	}

	public void addVariableType(final Integer variableType) {
		this.variableTypeIds.add(variableType);
	}

	public List<String> getPropertyClasses() {
		return this.propertyClasses;
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
		return this.names;
	}

	public List<Integer> getDatasetIds() {
		return this.datasetIds;
	}

	public void addDatasetId(final Integer datasetId) {
		this.datasetIds.add(datasetId);
	}

	public List<String> getGermplasmUUIDs() {
		return this.germplasmUUIDs;
	}

	public void addGermplasmUUID(final String germplasmUUIDs) {
		this.germplasmUUIDs.add(germplasmUUIDs);
	}

	public List<Integer> getLotIds() {
		return this.lotIds;
	}

	public void addLotId(final Integer lotId) {
		this.lotIds.add(lotId);
	}

	public boolean isShowObsoletes() {
		return this.showObsoletes;
	}

	public void setShowObsoletes(final boolean showObsoletes) {
		this.showObsoletes = showObsoletes;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);

	}
}
