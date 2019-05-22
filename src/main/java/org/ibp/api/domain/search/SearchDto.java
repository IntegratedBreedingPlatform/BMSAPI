package org.ibp.api.domain.search;

import org.pojomatic.Pojomatic;

public class SearchDto {

	private int searchResultDbId;

	public SearchDto(final int searchResultDbId) {
		super();
		this.searchResultDbId = searchResultDbId;
	}

	public int getSearchResultDbId() {
		return this.searchResultDbId;
	}

	public void setSearchResultDbId(final int searchResultDbId) {
		this.searchResultDbId = searchResultDbId;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
