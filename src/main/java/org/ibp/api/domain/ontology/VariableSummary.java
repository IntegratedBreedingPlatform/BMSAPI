package org.ibp.api.domain.ontology;

import org.generationcp.middleware.domain.oms.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Contains basic data used for list, insert and update of variable
 * Extended from {@link TermSummary} for getting basic fields like id, name and description
 */
public class VariableSummary extends TermSummary {

	private String alias;
	private TermSummary propertySummary;
	private TermSummary methodSummary;
	private ScaleSummary scaleSummary;
	private List<IdName> variableTypes = new ArrayList<>();
	private boolean favourite;
	private final ExpectedRange expectedRange = new ExpectedRange();
	private List<String> variableTypeIds;
	private String programUuid;
	private MetadataSummary metadata = new MetadataSummary();

	public String getProgramUuid() {
		return programUuid;
	}

	public void setProgramUuid(String programUuid) {
		this.programUuid = programUuid;
	}

	public MetadataSummary getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataSummary metadata) {
		this.metadata = metadata;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public TermSummary getPropertySummary() {
		return this.propertySummary;
	}

	public void setPropertySummary(TermSummary propertySummary) {
		this.propertySummary = propertySummary;
	}

	public TermSummary getMethodSummary() {
		return this.methodSummary;
	}

	public void setMethodSummary(TermSummary methodSummary) {
		this.methodSummary = methodSummary;
	}

	public ScaleSummary getScaleSummary() {
		return this.scaleSummary;
	}

	public void setScaleSummary(ScaleSummary scaleSummary) {
		this.scaleSummary = scaleSummary;
	}

	public List<IdName> getVariableTypes() {
		return this.variableTypes;
	}

	public List<String> getVariableTypeIds() {
		return variableTypeIds;
	}

	public void setVariableTypeIds(List<String> variableTypeIds) {
		this.variableTypeIds = variableTypeIds;
	}

	public void setVariableTypes(Set<VariableType> variables) {
		// Note: clear list if any exist
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

	public ExpectedRange getExpectedRange() {
		return this.expectedRange;
	}

	public void setExpectedMin(String min) {
		this.expectedRange.setMin(min);
	}

	public void setExpectedMax(String max) {
		this.expectedRange.setMax(max);
	}

	@Override
	public String toString() {
		return "VariableSummary{" +
				"alias='" + alias + '\'' +
				", propertySummary=" + propertySummary +
				", methodSummary=" + methodSummary +
				", scaleSummary=" + scaleSummary +
				", variableTypes=" + variableTypes +
				", favourite=" + favourite +
				", expectedRange=" + expectedRange +
				"} " + super.toString();
	}
}
