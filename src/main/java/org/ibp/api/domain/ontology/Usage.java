
package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * List of variables that used in Method, Property and Scale
 * Variable Observations and Studies
 * List of variable usage and variable observation and studies are mutually exclusive
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Usage {

	private final List<TermSummary> variables = new ArrayList<>();

	// observations of variable
	private Integer observations;

	// studies of variable
	private Integer studies;

	public void addUsage(TermSummary variable) {
		// Note: Do not add null value of variable
		if (variable != null) {
			this.variables.add(variable);
		}
	}

	public List<TermSummary> getVariables() {
		return variables;
	}

	public Integer getObservations() {
		return observations;
	}

	public void setObservations(Integer observations) {
		this.observations = observations;
	}

	public Integer getStudies() {
		return studies;
	}

	public void setStudies(Integer studies) {
		this.studies = studies;
	}

}
