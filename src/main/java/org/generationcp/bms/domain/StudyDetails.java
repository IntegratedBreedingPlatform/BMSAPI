package org.generationcp.bms.domain;

import java.util.ArrayList;
import java.util.List;

public class StudyDetails extends StudySummary {
	
	private final List<Trait> measuredTraits = new ArrayList<>();
	
	private final List<Variable> factors = new ArrayList<Variable>();
		
	public StudyDetails(int id) {
		super(id);
	}
	
	public List<Trait> getMeasuredTraits() {
		return measuredTraits;
	}
	
	public List<Variable> getFactors() {
		return factors;
	}

	public void addFactor(Variable factor) {
		if(factor != null) {
			factors.add(factor);
		}
	}
		
	public void addMeasuredTraits(List<Trait> traits) {
		if(traits != null) {
			measuredTraits.addAll(traits);
		}
	}
}

