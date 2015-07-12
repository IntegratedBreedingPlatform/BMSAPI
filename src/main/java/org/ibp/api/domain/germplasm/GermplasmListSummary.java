
package org.ibp.api.domain.germplasm;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class GermplasmListSummary {

	private Integer listId;
	private String listName;
	private String description;
	private String notes;

	private int listSize;
	private String listDetailsUrl;

	public Integer getListId() {
		return this.listId;
	}

	public void setListId(Integer listId) {
		this.listId = listId;
	}

	public String getListName() {
		return this.listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return this.notes;
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
		return this.listDetailsUrl;
	}

	public void setListDetailsUrl(String listDetailsUrl) {
		this.listDetailsUrl = listDetailsUrl;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof GermplasmListSummary)) {
			return false;
		}
		GermplasmListSummary castOther = (GermplasmListSummary) other;
		return new EqualsBuilder().append(this.listId, castOther.listId).append(this.listName, castOther.listName)
				.append(this.description, castOther.description).append(this.notes, castOther.notes)
				.append(this.listSize, castOther.listSize).append(this.listDetailsUrl, castOther.listDetailsUrl).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.listId).append(this.listName).append(this.description).append(this.notes)
				.append(this.listSize).append(this.listDetailsUrl).toHashCode();
	}

}
