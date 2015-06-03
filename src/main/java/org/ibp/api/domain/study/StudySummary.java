
package org.ibp.api.domain.study;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Summary information about a study (Trials and Nurseries).
 *
 */
public class StudySummary {

	private String id;
	private String name;
	private String title;
	private String objective;
	private String type;
	private String startDate;
	private String endDate;

	public StudySummary() {

	}

	public StudySummary(String studyId) {
		this.id = studyId;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getObjective() {
		return this.objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return "StudySummary [id=" + this.id + ", name=" + this.name + ", title=" + this.title + ", type=" + this.type + "]";
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StudySummary)) {
			return false;
		}
		StudySummary castOther = (StudySummary) other;
		return new EqualsBuilder().append(this.id, castOther.id).append(this.name, castOther.name).append(this.title, castOther.title)
				.append(this.objective, castOther.objective).append(this.type, castOther.type).append(this.startDate, castOther.startDate)
				.append(this.endDate, castOther.endDate).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).append(this.name).append(this.title).append(this.objective).append(this.type)
				.append(this.startDate).append(this.endDate).toHashCode();
	}
}
