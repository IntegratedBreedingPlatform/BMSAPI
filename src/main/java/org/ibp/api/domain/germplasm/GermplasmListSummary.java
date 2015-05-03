
package org.ibp.api.domain.germplasm;

public class GermplasmListSummary {

	private Integer listId;
	private String listName;
	private String description;
	private String notes;

	private int listSize;
	private String listDetailsUrl;

	public Integer getListId() {
		return listId;
	}

	public void setListId(Integer listId) {
		this.listId = listId;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getListSize() {
		return this.listSize;
	}

	public void setListSize(int listSize) {
		this.listSize = listSize;
	}

	public String getListDetailsUrl() {
		return listDetailsUrl;
	}

	public void setListDetailsUrl(String listDetailsUrl) {
		this.listDetailsUrl = listDetailsUrl;
	}
}
