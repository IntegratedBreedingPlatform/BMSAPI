package org.ibp.api.brapi.v1.program;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"programDbId", "name", "name", "abbreviation", "objective", "leadPerson"})
public class Program {

	private Integer programDbId;
	private String name;
	private String abbreviation;
	private String objective;
	private String leadPerson;

	public Program() {

	}

	public Program(final Integer programDbId, final String name, final String abbreviation, final String objective,
		final String leadPerson) {

		this.programDbId = programDbId;
		this.name = name;
		this.abbreviation = abbreviation;
		this.objective = objective;
		this.leadPerson = leadPerson;
	}

	public Integer getProgramDbId() {
		return programDbId;
	}

	public void setProgramDbId(Integer programDbId) {
		this.programDbId = programDbId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getLeadPerson() {
		return leadPerson;
	}

	public void setLeadPerson(String leadPerson) {
		this.leadPerson = leadPerson;
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
