package org.ibp.api.rest.samplesubmission.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.List;

@AutoProperty
public class GOBiiSampleList {

	private Integer projectId;

	private List<GOBiiSample> samples;

	public GOBiiSampleList(){
		samples = new ArrayList<>();
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(final Integer projectId) {
		this.projectId = projectId;
	}

	public List<GOBiiSample> getSamples() {
		return samples;
	}

	public void setSamples(final List<GOBiiSample> samples) {
		this.samples = samples;
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
