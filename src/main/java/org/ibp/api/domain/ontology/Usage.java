
package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

public class Usage {

	private final List<TermSummary> variables = new ArrayList<>();

	public void addUsage(TermSummary variable) {
		if (variable != null) {
			this.variables.add(variable);
		}
	}

	public List<TermSummary> getVariables() {
		return variables;
	}

}
