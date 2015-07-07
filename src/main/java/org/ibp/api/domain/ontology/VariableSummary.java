
package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.ibp.api.domain.ontology.serializers.VariableSummarySerializer;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains basic data used for list, insert and update of variable Extended from {@link TermSummary} for getting basic fields like id, name
 * and description
 */

@JsonSerialize(using = VariableSummarySerializer.class)
public class VariableSummary extends TermSummary {

	private String alias;
	private String programUuid;
	private TermSummary propertySummary = new TermSummary();
	private TermSummary methodSummary = new TermSummary();

	// Note : ScaleSummary that ignore elements except TermSummary and DataType
	private ScaleSummary scaleSummary = new ScaleSummary();
	private final Set<VariableType> variableTypes = new HashSet<>();
	private boolean favourite;
	private final ExpectedRange expectedRange = new ExpectedRange();
	private MetadataSummary metadata = new MetadataSummary();

	public String getProgramUuid() {
		return this.programUuid;
	}

	public void setProgramUuid(String programUuid) {
		this.programUuid = programUuid;
	}

	public MetadataSummary getMetadata() {
		return this.metadata;
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

	public Set<VariableType> getVariableTypes() {
		return this.variableTypes;
	}

	public void setVariableTypes(Set<VariableType> variables) {
		// Note: clear list if any exist
		this.variableTypes.clear();

		if (variables == null) {
			return;
		}

		for (VariableType v : variables) {
			this.variableTypes.add(new VariableType(v.getId(), v.getName(), v.getDescription()));
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

	@JsonIgnore
	public void setExpectedMin(String min) {
		this.expectedRange.setMin(min);
	}

	@JsonIgnore
	public void setExpectedMax(String max) {
		this.expectedRange.setMax(max);
	}

	@Override
	public String toString() {
		return "VariableSummary{" + "alias='" + this.alias + '\'' + ", propertySummary=" + this.propertySummary + ", methodSummary="
				+ this.methodSummary + ", scaleSummary=" + this.scaleSummary + ", variableTypes=" + this.variableTypes + ", favourite="
				+ this.favourite + ", expectedRange=" + this.expectedRange + "} " + super.toString();
	}
}
