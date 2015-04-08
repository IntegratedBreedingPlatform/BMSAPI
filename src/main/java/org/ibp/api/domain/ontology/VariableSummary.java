package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.oms.VariableType;

public class VariableSummary {

	// TODO : Need to fetch alias and usage

	private Integer id;
	private String name;
	private String alias;
	private String description;
	private IdName propertySummary;
	private IdName methodSummary;
	private IdName scaleSummary;
	private List<IdName> variableTypes;
	private boolean favourite;
	private MetaData metadata;
	private ExpectedRange expectedRange;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public IdName getPropertySummary() {
		return this.propertySummary;
	}

	public void setPropertySummary(IdName propertySummary) {
		this.propertySummary = propertySummary;
	}

	public IdName getMethodSummary() {
		return this.methodSummary;
	}

	public void setMethodSummary(IdName methodSummary) {
		this.methodSummary = methodSummary;
	}

	public IdName getScaleSummary() {
		return this.scaleSummary;
	}

	public void setScaleSummary(IdName scaleSummary) {
		this.scaleSummary = scaleSummary;
	}

	public List<IdName> getVariableTypes() {
		return this.variableTypes;
	}

	public void setVariableTypes(Set<VariableType> variables) {
		if (this.variableTypes == null) {
			this.variableTypes = new ArrayList<>();
		}

		this.variableTypes.clear();
		for (VariableType v : variables) {
			this.variableTypes.add(new IdName(v.getId(), v.getName()));
		}
	}

	public boolean isFavourite() {
		return this.favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	public MetaData getMetadata() {
		return this.metadata;
	}

	public ExpectedRange getExpectedRange() {
		return this.expectedRange;
	}

	public void setExpectedMin(String min) {
		this.ensureExpectedRangeInitialized();
		this.expectedRange.setMin(min);
	}

	public void setExpectedMax(String max) {
		this.ensureExpectedRangeInitialized();
		this.expectedRange.setMax(max);
	}

	public void setModifiedData(Date modifiedDate) {
		this.ensureMetaDataInitialized();
		this.metadata.setDateLastModified(modifiedDate);
	}

	public void setCreatedDate(Date createdDate) {
		this.ensureMetaDataInitialized();
		this.metadata.setDateCreated(createdDate);
	}

	public void setObservations(Integer observations) {
		this.ensureMetaDataInitialized();
		this.metadata.setObservations(observations);
	}

	private void ensureMetaDataInitialized() {
		if (this.metadata == null) {
			this.metadata = new MetaData();
		}
	}

	private void ensureExpectedRangeInitialized() {
		if (this.expectedRange == null) {
			this.expectedRange = new ExpectedRange();
		}
	}

	@Override
	public String toString() {
		return "Variable [" + "id=" + this.id + ", name='" + this.name + '\'' + ", alias='"
				+ this.alias + '\'' + ", description='" + this.description + '\''
				+ ", propertySummary=" + this.propertySummary + ", methodSummary="
				+ this.methodSummary + ", scaleSummary=" + this.scaleSummary + ", variableTypes="
				+ this.variableTypes + ", favourite=" + this.favourite + ", metadata="
				+ this.metadata + ", expectedRange=" + this.expectedRange + ']';
	}
}
