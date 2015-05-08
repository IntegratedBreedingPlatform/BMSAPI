
package org.ibp.api.domain.study;

/**
 * Summary information about a study (Trials and Nurseries).
 *
 */
public class StudySummary {

	private Integer id;
	private String name;
	private String title;
	private String objective;
	private String type;
	private String startDate;
	private String endDate;

	public StudySummary() {

	}

	public StudySummary(Integer studyId) {
		this.id = studyId;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
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
}
