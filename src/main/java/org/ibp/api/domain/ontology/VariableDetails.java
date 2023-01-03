
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
	private boolean obsolete;

	public MetadataDetails getMetadata() {
		return this.metadata;
	}

	public void setMetadata(final MetadataDetails metadata) {
		this.metadata = metadata;
	}

	public ExpectedRange getExpectedRange() {
		return this.expectedRange;
	}

	public String getProgramUuid() {
		return this.programUuid;
	}

	public void setProgramUuid(final String programUuid) {
		this.programUuid = programUuid;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(final String alias) {
		this.alias = alias;
	}

	public MethodDetails getMethod() {
		return this.method;
	}

	public void setMethod(final MethodDetails method) {
		this.method = method;
	}

	public PropertyDetails getProperty() {
		return this.property;
	}

	public void setProperty(final PropertyDetails property) {
		this.property = property;
	}

	public ScaleDetails getScale() {
		return this.scale;
	}

	public void setScale(final ScaleDetails scale) {
		this.scale = scale;
	}

	public boolean isFavourite() {
		return this.favourite;
	}

	public void setFavourite(final boolean favourite) {
		this.favourite = favourite;
	}

	public void setExpectedMin(final String min) {
		this.expectedRange.setMin(min);
	}

	public void setExpectedMax(final String max) {
		this.expectedRange.setMax(max);
	}

	public List<VariableType> getVariableTypes() {
		return this.variableTypes;
	}

	public void setVariableTypes(final Set<VariableType> variables) {
		this.variableTypes.clear();

		if (variables == null) {
			return;
		}

		for (final VariableType variableType : variables) {
			this.variableTypes.add(variableType);
		}
	}

	public void setObservations(final Integer observations) {
		if (observations == null) {
			this.metadata.setObservations(0);
		} else {
			this.metadata.setObservations(observations);
		}
	}

	public void setStudies(final Integer studies) {
		if (studies == null) {
			this.metadata.setStudies(0);
		} else {
			this.metadata.setStudies(studies);
		}
	}

	public FormulaDto getFormula() {
		return this.formula;
	}

	public void setFormula(final FormulaDto formula) {
		this.formula = formula;
	}

	public boolean isAllowsFormula() {
		return this.allowsFormula;
	}

	public void setAllowsFormula(final boolean allowsFormula) {
		this.allowsFormula = allowsFormula;
	}

	public boolean hasVariableType(final String variableTypeName) {
		return this.getVariableTypes().stream().anyMatch(variableType -> variableType.getName().equals(variableTypeName));
	}

	public boolean isObsolete() {
		return this.obsolete;
	}

	public void setObsolete(final boolean obsolete) {
		this.obsolete = obsolete;
	}

}
