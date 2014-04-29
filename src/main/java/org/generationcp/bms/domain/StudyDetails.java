package org.generationcp.bms.domain;

import java.util.ArrayList;
import java.util.List;

public class StudyDetails extends StudySummary {
	
	//Better name? metaData, studyInfo, properties?
	private final List<Variable> factors = new ArrayList<Variable>();
	
	private final List<Variable> traits = new ArrayList<Variable>();
	
	public StudyDetails(int id) {
		super(id);
	}
	
	public void addFactor(Variable factor) {
		if(factor != null) {
			factors.add(factor);
		}
	}
	
	public void addTrait(Variable trait) {
		if(trait != null) {
			traits.add(trait);
		}
	}

	public List<Variable> getFactors() {
		return factors;
	}

	public List<Variable> getTraits() {
		return traits;
	}
}

