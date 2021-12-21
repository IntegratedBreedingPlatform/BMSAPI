
package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * List of variables that used in Method, Property and Scale Variable Observations and Studies List of variable usage and variable
 * observation and studies are mutually exclusive
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Usage {

	private final List<TermSummary> variables = new ArrayList<>();

	// observations of variable
	private Integer observations;

	// studies of variable
	private Integer studies;

	// datasets of variable
	private Integer datasets;

	// germplasms of variable
	private Integer germplasm;

	// breeding methods of variable
	private Integer breedingMethods;

	// lists of variable
	private Integer lists;

	private boolean isSystemVariable;

	public void addUsage(final TermSummary variable) {
		// Note: Do not add null value of variable
		if (variable != null) {
			this.variables.add(variable);
		}
	}

	public List<TermSummary> getVariables() {
		return this.variables;
	}

	public Integer getObservations() {
		return this.observations;
	}

	public void setObservations(final Integer observations) {
		this.observations = observations;
	}

	public Integer getStudies() {
		return this.studies;
	}

	public void setStudies(final Integer studies) {
		this.studies = studies;
	}

	public Integer getDatasets() {
		return this.datasets;
	}

	public void setDatasets(final Integer datasets) {
		this.datasets = datasets;
	}

	public Integer getGermplasm() {
		return this.germplasm;
	}

	public void setGermplasm(final Integer germplasm) {
		this.germplasm = germplasm;
	}

	public Integer getBreedingMethods() {
		return this.breedingMethods;
	}

	public void setBreedingMethods(final Integer breedingMethods) {
		this.breedingMethods = breedingMethods;
	}

	public Integer getLists() {
		return lists;
	}

	public void setLists(final Integer lists) {
		this.lists = lists;
	}

	public boolean isSystemVariable() {
		return this.isSystemVariable;
	}

	public void setSystemVariable(final boolean systemVariable) {
		this.isSystemVariable = systemVariable;
	}
}
