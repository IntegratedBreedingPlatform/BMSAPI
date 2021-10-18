package org.ibp.api.domain.search;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class BrapiSearchDto {

	private String searchResultsDbId;

	public BrapiSearchDto(final String searchResultsDbId) {
		super();
		this.searchResultsDbId = searchResultsDbId;
	}

	public String getSearchResultsDbId() {
		return this.searchResultsDbId;
	}

	public void setSearchResultsDbId(final String searchResultsDbId) {
		this.searchResultsDbId = searchResultsDbId;
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
