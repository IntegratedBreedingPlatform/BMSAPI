package org.ibp.api.brapi.v1.observation;

import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.v1.common.Result;

import java.util.List;

public class ObservationVariableResult extends Result<VariableDTO> {

	private int studyDbId;
	private String trialName;

	public int getStudyDbId() {
		return this.studyDbId;
	}

	public void setStudyDbId(final int studyDbId) {
		this.studyDbId = studyDbId;
	}

	public String getTrialName() {
		return this.trialName;
	}

	public void setTrialName(final String trialName) {
		this.trialName = trialName;
	}

	public ObservationVariableResult withStudyDbId(final int studyId) {
		this.studyDbId = studyId;
		return this;
	}

	public ObservationVariableResult withTrialName(final String trialName) {
		this.trialName = trialName;
		return this;
	}

	@Override
	public ObservationVariableResult withData(final List<VariableDTO> data) {
		this.setData(data);
		return this;
	}

}
