package org.ibp.api.domain.ontology;

import org.generationcp.middleware.domain.oms.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VariableSummary extends AuditTermSummary {

	private String alias;
	private IdName propertySummary;
	private IdName methodSummary;
	private IdName scaleSummary;
	private List<IdName> variableTypes;
	private boolean favourite;
	private final ExpectedRange expectedRange = new ExpectedRange();

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
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
