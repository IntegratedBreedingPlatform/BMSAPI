
package org.ibp.api.domain.program;

/**
 * Summary information about breeding program.
 *
 */
public class ProgramSummary {

	private String id;
	private String uniqueID;
	private String projectName;
	private String userId;
	private String cropType;
	private String startDate;

	public ProgramSummary() {
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUniqueID() {
		return this.uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCropType() {
		return this.cropType;
	}

	public void setCropType(String cropType) {
		this.cropType = cropType;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
}
