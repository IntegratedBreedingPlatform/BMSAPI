
package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.ibp.api.domain.ontology.serializers.VariableDetailsSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Holds all Variable details. Extended from {@link TermSummary} for basic term details.
 */

@JsonSerialize(using = VariableDetailsSerializer.class)
public class VariableDetails extends TermSummary {

	private MetadataDetails metadata = new MetadataDetails();

	private String programUuid;
	private String alias;
	private MethodDetails method;
	private PropertyDetails property;
	private ScaleDetails scale;
	private final List<VariableType> variableTypes = new ArrayList<>();
	private boolean favourite;
	private final ExpectedRange expectedRange = new ExpectedRange();
	private FormulaDto formula;
	private boolean allowsFormula = false;

	public MetadataDetails getMetadata() {
		return this.metadata;
	}

	public void setMetadata(MetadataDetails metadata) {
		this.metadata = metadata;
	}

	public ExpectedRange getExpectedRange() {
		return this.expectedRange;
	}

	public String getProgramUuid() {
		return programUuid;
	}

	public void setProgramUuid(String programUuid) {
		this.programUuid = programUuid;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public MethodDetails getMethod() {
		return method;
	}

	public void setMethod(MethodDetails method) {
		this.method = method;
	}

	public PropertyDetails getProperty() {
		return property;
	}

	public void setProperty(PropertyDetails property) {
		this.property = property;
	}

	public ScaleDetails getScale() {
		return this.scale;
	}

	public void setScale(ScaleDetails scale) {
		this.scale = scale;
	}

	public boolean isFavourite() {
		return this.favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	public void setExpectedMin(String min) {
		this.expectedRange.setMin(min);
	}

	public void setExpectedMax(String max) {
		this.expectedRange.setMax(max);
	}

	public List<VariableType> getVariableTypes() {
		return this.variableTypes;
	}

	public void setVariableTypes(Set<VariableType> variables) {
		this.variableTypes.clear();

		if (variables == null) {
			return;
		}

		for (VariableType variableType : variables) {
			this.variableTypes.add(variableType);
		}
	}

	public void setObservations(Integer observations) {
		if (observations == null) {
			this.metadata.setObservations(0);
		} else {
			this.metadata.setObservations(observations);
		}
	}

	public void setStudies(Integer studies) {
		if (studies == null) {
			this.metadata.setStudies(0);
		} else {
			this.metadata.setStudies(studies);
		}
	}

	public FormulaDto getFormula() {
		return formula;
	}

	public void setFormula(final FormulaDto formula) {
		this.formula = formula;
	}

	public boolean isAllowsFormula() {
		return allowsFormula;
	}

	public void setAllowsFormula(final boolean allowsFormula) {
		this.allowsFormula = allowsFormula;
	}

	public boolean hasVariableType(final String variableTypeName) {
		return this.getVariableTypes().stream().anyMatch(variableType -> variableType.getName().equals(variableTypeName));
	}

}
