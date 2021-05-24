package org.ibp.api.brapi.v2.trial;

import org.generationcp.middleware.domain.dms.StudySummary;
import org.springframework.validation.ObjectError;

import java.util.List;

public class TrialImportResponse {
	private String status;
	private List<ObjectError> errors;
	private List<StudySummary> studySummaries;

	public TrialImportResponse() {

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public List<ObjectError> getErrors() {
		return errors;
	}

	public void setErrors(final List<ObjectError> errors) {
		this.errors = errors;
	}

	public List<StudySummary> getStudySummaries() {
		return this.studySummaries;
	}

	public void setStudySummaries(final List<StudySummary> studySummaries) {
		this.studySummaries = studySummaries;
	}
}
