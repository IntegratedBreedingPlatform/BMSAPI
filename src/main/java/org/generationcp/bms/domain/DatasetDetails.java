package org.generationcp.bms.domain;

import java.util.ArrayList;
import java.util.List;

public class DatasetDetails extends DatasetSummary {
	
	private String studySummaryUrl;
	
	private final List<Trait> measuredTraits = new ArrayList<Trait>();
	
	/**
	 * Traits with numeric measurement values only.
	 */
	public List<Trait> getNumericTraits() {
		List<Trait> numericTraits = new ArrayList<Trait>();
		for(Trait trait : measuredTraits) {
			if(trait.isNumeric()) {
				numericTraits.add(trait);
			}
		}
		return numericTraits;
	}
	
	/**
	 * All traits that have measurements recorded.
	 */
	public List<Trait> getMeasuredTraits() {
		return measuredTraits;
	}
		
	public void addMeasuredTraits(List<Trait> traits) {
		if(traits != null) {
			measuredTraits.addAll(traits);
		}
	}

	public String getStudySummaryUrl() {
		return studySummaryUrl;
	}

	public void setStudySummaryUrl(String studySummaryUrl) {
		this.studySummaryUrl = studySummaryUrl;
	}

}
