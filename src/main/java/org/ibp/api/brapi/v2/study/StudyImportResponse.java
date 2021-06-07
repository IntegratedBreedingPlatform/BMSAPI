package org.ibp.api.brapi.v2.study;

import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;
import org.springframework.validation.ObjectError;

import java.util.List;

@AutoProperty
public class StudyImportResponse {

	private String status;
	private List<ObjectError> errors;
	private List<StudyInstanceDto> studyInstanceDtos;

	public StudyImportResponse() {

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

	public List<StudyInstanceDto> getStudyInstanceDtos() {
		return this.studyInstanceDtos;
	}

	public void setStudyInstanceDtos(final List<StudyInstanceDto> studyInstanceDtos) {
		this.studyInstanceDtos = studyInstanceDtos;
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

}
