package org.ibp.api.domain.search;

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
	public String toString() {
		return "SearchDto{" +
			"searchResultDbId=" + this.searchResultDbId +
			'}';
	}
}
