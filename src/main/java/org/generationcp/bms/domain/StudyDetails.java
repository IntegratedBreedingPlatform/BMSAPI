package org.generationcp.bms.domain;

import java.util.ArrayList;
import java.util.List;

public class StudyDetails extends StudySummary {

	private List<NameValuePair> variables = new ArrayList<NameValuePair>();
	
	public StudyDetails(int id) {
		super(id);
	}
	
	public List<NameValuePair> getVariables() {
		return variables;
	}
	
	public void addVariable(String name, String value) {
		variables.add(new NameValuePair(name, value));
	}
}

