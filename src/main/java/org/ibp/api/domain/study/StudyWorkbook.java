package org.ibp.api.domain.study;

import org.ibp.api.rest.study.StudyResource;

/**
 * Front end bean for receiving data in saveStudy() method in {@link StudyResource} 
 * @author j-alberto
 *
 */
public class StudyWorkbook {

	private String name;
	private String objective;
	private String startDate;
	private String endDate;
	private String title;
	private String studyType;
	private String siteName;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getObjective() {
		return objective;
	}
	public void setObjective(String objective) {
		this.objective = objective;
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStudyType() {
		return studyType;
	}
	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
}
