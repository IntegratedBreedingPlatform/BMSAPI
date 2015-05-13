
package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

/**
 * List of variables that used in Method, Property and Scale
 * Variable Observations and Studies
 * List of variable usage and variable observation and studies are mutually exclusive
 */
public class Usage {

	private final List<TermSummary> variables = new ArrayList<>();

	//TODO: add observation and studies

	public void addUsage(TermSummary variable) {
		// Note: Do not add null value of variable
		if (variable != null) {
			this.variables.add(variable);
		}
	}

	public List<TermSummary> getVariables() {
		return variables;
	}

}
