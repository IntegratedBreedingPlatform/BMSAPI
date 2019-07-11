package org.ibp.api.domain.search;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class SearchDto {

	private String searchResultDbId;

	public SearchDto(final String searchResultDbId) {
		super();
		this.searchResultDbId = searchResultDbId;
	}

	public String getSearchResultDbId() {
		return this.searchResultDbId;
	}

	public void setSearchResultDbId(final String searchResultDbId) {
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
