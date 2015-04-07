
package org.ibp.api.domain;

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

	public StudySummary(int id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return "StudySummary [id=" + id + ", name=" + name + ", title=" + title + ", type=" + type + "]";
	}
}
