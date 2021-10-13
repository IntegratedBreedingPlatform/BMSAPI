package org.ibp.api.domain.search;

import com.fasterxml.jackson.annotation.JsonView;
import org.generationcp.middleware.service.api.BrapiView;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class SearchDto {

	@JsonView({BrapiView.BrapiV1_2.class, BrapiView.BrapiV1_3.class})
	private String searchResultDbId;

	private String searchResultsDbId;

	public SearchDto(final String searchResultsDbId) {
		super();
		this.searchResultsDbId = searchResultsDbId;
	}

	public SearchDto(final String searchResultsDbId, boolean isV1) {
		super();
		if (isV1) {
			this.searchResultDbId = searchResultsDbId;
		}
		this.searchResultsDbId = searchResultsDbId;
	}

	public String getSearchResultsDbId() {
		return this.searchResultsDbId;
	}

	public void setSearchResultsDbId(final String searchResultsDbId) {
		this.searchResultsDbId = searchResultsDbId;
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
