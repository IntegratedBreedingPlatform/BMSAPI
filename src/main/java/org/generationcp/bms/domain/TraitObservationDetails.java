package org.generationcp.bms.domain;

import java.util.ArrayList;
import java.util.List;

public class TraitObservationDetails {

	private final int traitId;
	private final int studyId;
	
	private final List<TraitObservation> observations = new ArrayList<TraitObservation>();

	public TraitObservationDetails(int traitId, int studyId) {
		this.traitId = traitId;
		this.studyId = studyId;
	}

	public void addObservations(List<TraitObservation> observations) {
		if (observations != null) {
			this.observations.addAll(observations);
		}
	}

	public int getTraitId() {
		return traitId;
	}

	public int getStudyId() {
		return studyId;
	}

	public int getTotalObservations() {
		return observations.size();
	}
	
	public List<TraitObservation> getObservations() {
		return observations;
	}
}
