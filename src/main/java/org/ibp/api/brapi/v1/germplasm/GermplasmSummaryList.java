package org.ibp.api.brapi.v1.germplasm;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class GermplasmSummaryList {

	private List<Germplasm> data;

	private String studyDbId;

	private String trialName;

	public GermplasmSummaryList() {
	}

	public GermplasmSummaryList(final List<Germplasm> data, final String studyDbId, final String trialName) {
		this.data = data;
		this.studyDbId = studyDbId;
		this.trialName = trialName;
	}

	public List<Germplasm> getData() {
		return data;
	}

	public void setData(final List<Germplasm> data) {
		this.data = data;
	}

	public String getStudyDbId() {
		return studyDbId;
	}

	public void setStudyDbId(final String studyDbId) {
		this.studyDbId = studyDbId;
	}

	public String getTrialName() {
		return trialName;
	}

	public void setTrialName(final String trialName) {
		this.trialName = trialName;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
