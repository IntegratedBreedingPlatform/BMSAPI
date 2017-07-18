
package org.ibp.api.brapi.v1.trial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.ibp.api.brapi.v1.study.StudySummaryDto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrialSummary {

	private Integer trialDbId;

	private String trialName;

	private String programDbId;

	private String programName;

	private String startDate;

	private String endDate;

	private String locationDbId;

	private boolean active;

	private List<StudySummaryDto> studies = new ArrayList<>();

	private Map<String, String> additionalInfo = new HashMap<String, String>();

	public TrialSummary() {

	}

	public Integer getTrialDbId() {
		return this.trialDbId;
	}

	public void setTrialDbId(final Integer trialDbId) {
		this.trialDbId = trialDbId;
	}

	public String getTrialName() {
		return this.trialName;
	}

	public void setTrialName(final String trialName) {
		this.trialName = trialName;
	}

	public String getProgramDbId() {
		return this.programDbId;
	}

	public String getProgramName() {
		return this.programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public void setProgramDbId(final String programDbId) {
		this.programDbId = programDbId;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public List<StudySummaryDto> getStudies() {
		return this.studies;
	}

	public void setStudies(List<StudySummaryDto> studies) {
		this.studies = studies;
	}

	public void addStudy(final StudySummaryDto study) {
		this.studies.add(study);
	}

	public Map<String, String> getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public void addAdditionalInfo(final String name, final String value) {
		this.additionalInfo.put(name, value);
	}

	public String getLocationDbId() {
		return locationDbId;
	}

	public TrialSummary setLocationDbId(final String locationDbId) {
		this.locationDbId = locationDbId;
		return this;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof TrialSummary)) {
			return false;
		}
		if (other == this) {
			return true;
		}

		final TrialSummary castOther = (TrialSummary) other;
		return new EqualsBuilder().append(this.getTrialDbId(), castOther.getTrialDbId()) //
			.append(this.getProgramDbId(),castOther.getProgramDbId()) //
			.append(this.getProgramName(),castOther.getProgramName()) //
			.append(this.getStartDate(),castOther.getStartDate()) //
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.trialDbId).append(this.programDbId).append(this.programName).append(this.startDate)
			.hashCode();
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}
