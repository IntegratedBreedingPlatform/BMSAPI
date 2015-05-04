
package org.ibp.api.domain.germplasm;

public class GermplasmListEntrySummary {

	private Integer gid;
	private String designation;
	private String seedSource;
	private String entryCode;
	private String cross;

	public Integer getGid() {
		return gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getSeedSource() {
		return seedSource;
	}

	public void setSeedSource(String seedSource) {
		this.seedSource = seedSource;
	}

	public String getEntryCode() {
		return entryCode;
	}

	public void setEntryCode(String entryCode) {
		this.entryCode = entryCode;
	}

	public String getCross() {
		return cross;
	}

	public void setCross(String cross) {
		this.cross = cross;
	}

}
