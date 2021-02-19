package org.ibp.api.brapi.v1.program;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.generationcp.middleware.service.api.BrapiView;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"programDbId", "name", "programName", "abbreviation", "objective", "leadPerson"})
public class Program {

	private String programDbId;
	private String abbreviation;
	private String objective;

	@JsonView({BrapiView.BrapiV1_2.class, BrapiView.BrapiV1_3.class})
	private String name;
	@JsonView({BrapiView.BrapiV1_2.class, BrapiView.BrapiV1_3.class})
	private String leadPerson;

	@JsonView(BrapiView.BrapiV2.class)
	private String programName;
	@JsonView(BrapiView.BrapiV2.class)
	private String leadPersonDbId;
	@JsonView(BrapiView.BrapiV2.class)
	private String leadPersonName;
	@JsonView(BrapiView.BrapiV2.class)
	private String documentationURL;
	@JsonView(BrapiView.BrapiV2.class)
	private String externalReferences;
	@JsonView(BrapiView.BrapiV2.class)
	private String commonCropName;
	@JsonView(BrapiView.BrapiV2.class)
	private ProgramAdditionalInfo additionalInfo;

	public Program() {

	}

	public Program(final String programDbId, final String name, final String abbreviation, final String objective,
		final String leadPerson) {

		this.programDbId = programDbId;
		this.name = name;
		this.abbreviation = abbreviation;
		this.objective = objective;
		this.leadPerson = leadPerson;
	}

	public String getProgramDbId() {
		return this.programDbId;
	}

	public void setProgramDbId(final String programDbId) {
		this.programDbId = programDbId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	public void setAbbreviation(final String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getObjective() {
		return this.objective;
	}

	public void setObjective(final String objective) {
		this.objective = objective;
	}

	public String getLeadPerson() {
		return this.leadPerson;
	}

	public void setLeadPerson(final String leadPerson) {
		this.leadPerson = leadPerson;
	}

	public String getProgramName() {
		return this.programName;
	}

	public void setProgramName(final String programName) {
		this.programName = programName;
	}

	public String getLeadPersonDbId() {
		return this.leadPersonDbId;
	}

	public void setLeadPersonDbId(final String leadPersonDbId) {
		this.leadPersonDbId = leadPersonDbId;
	}

	public String getLeadPersonName() {
		return this.leadPersonName;
	}

	public void setLeadPersonName(final String leadPersonName) {
		this.leadPersonName = leadPersonName;
	}

	public String getDocumentationURL() {
		return this.documentationURL;
	}

	public void setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
	}

	public String getExternalReferences() {
		return this.externalReferences;
	}

	public void setExternalReferences(final String externalReferences) {
		this.externalReferences = externalReferences;
	}

	public String getCommonCropName() {
		return this.commonCropName;
	}

	public void setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
	}

	public ProgramAdditionalInfo getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(final ProgramAdditionalInfo additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Program)) {
			return false;
		}
		final Program castOther = (Program) other;
		return new EqualsBuilder().append(this.programDbId, castOther.programDbId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.programDbId).hashCode();
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}
