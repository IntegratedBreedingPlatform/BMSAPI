
package org.ibp.api.domain.study;

import org.ibp.api.domain.ontology.TermSummary;

public class StudyAttribute extends TermSummary {

	private String value;
	
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
