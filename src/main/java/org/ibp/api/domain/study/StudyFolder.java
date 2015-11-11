
package org.ibp.api.domain.study;

public class StudyFolder {

	private Integer folderId;
	private String name;
	private String description;
	private Integer parentFolderId;

	public StudyFolder() {
	}

	public StudyFolder(final Integer folderId, final String name, final String description, final Integer parentFolderId) {
		this.folderId = folderId;
		this.name = name;
		this.description = description;
		this.parentFolderId = parentFolderId;
	}

	public Integer getFolderId() {
		return this.folderId;
	}

	public void setFolderId(final Integer folderId) {
		this.folderId = folderId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Integer getParentFolderId() {
		return this.parentFolderId;
	}

	public void setParentFolderId(final Integer parentFolderId) {
		this.parentFolderId = parentFolderId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

}
