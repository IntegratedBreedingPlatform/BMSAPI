package org.ibp.api.brapi.v1.observation;

import org.generationcp.middleware.service.api.study.VariableDto;
import org.ibp.api.brapi.v1.common.Result;

import java.util.List;

public class ObservationVariableResult extends Result<VariableDto> {

	private int studyId;
	private String trialName;

	public int getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final int studyId) {
		this.studyId = studyId;
	}

	public String getTrialName() {
		return this.trialName;
	}

	public void setTrialName(final String trialName) {
		this.trialName = trialName;
	}

	public ObservationVariableResult withStudyId(final int studyId) {
		this.studyId = studyId;
		return this;
	}

	public ObservationVariableResult withTrialName(final String trialName) {
		this.trialName = trialName;
		return this;
	}

	@Override
	public ObservationVariableResult withData(final List<VariableDto> data) {
		this.setData(data);
		return this;
	}

}
