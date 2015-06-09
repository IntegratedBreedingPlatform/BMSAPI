package org.ibp.api.domain.workbench;

public class Program {

	String cropName;
	String programName;
	String programUuid;

	public Program(String cropName, String programUuid) {
		this.cropName = cropName;
		this.programUuid = programUuid;
	}

	public String getCropName() {
		return cropName;
	}

	public void setCropName(String cropName) {
		this.cropName = cropName;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getProgramUuid() {
		return programUuid;
	}

	public void setProgramUuid(String programUuid) {
		this.programUuid = programUuid;
	}
}
