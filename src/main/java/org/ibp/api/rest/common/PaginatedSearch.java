
package org.ibp.api.rest.common;

import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.domain.common.PagedResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Supplier;

/**
 * Contains the general pattern for executing paginated searches.
 *
 * @author Naymesh Mistry
 */
public class PaginatedSearch {

	public <T> PagedResult<T> execute(final Integer pageNumber, final Integer pageSize, final SearchSpec<T> searchSpec) {

		Integer pgeNum = pageNumber;
		// Default page parameters if not supplied.
		if (pgeNum == null) {
			pgeNum = new Integer(PagedResult.DEFAULT_PAGE_NUMBER);
		}

		Integer pgeSize = pageSize;
		if (pgeSize == null) {
			pgeSize = new Integer(PagedResult.DEFAULT_PAGE_SIZE);
		}

		final long totalResults = searchSpec.getCount();
		final long filteredResults = searchSpec.getFilteredCount();

		// Initialise page parameters/metadata and validate.
		final PagedResult<T> pagedResult = new PagedResult<T>(pgeNum, pgeSize, totalResults, filteredResults);

		// Add list/search result
		pagedResult.addPageResults(searchSpec.getResults(pagedResult));
		return pagedResult;
	}

	public <T> BrapiPagedResult<T> executeBrapiSearch(final Integer pageNumber, final Integer pageSize, final SearchSpec<T> searchSpec) {

		Integer pgeNum = pageNumber;
		// Default page parameters if not supplied.
		if (pgeNum == null) {
			pgeNum = new Integer(BrapiPagedResult.DEFAULT_PAGE_NUMBER);
		}

		Integer pgeSize = pageSize;
		if (pgeSize == null) {
			pgeSize = new Integer(BrapiPagedResult.DEFAULT_PAGE_SIZE);
		}

		final long totalResults = searchSpec.getCount();
		final long filteredResults = searchSpec.getFilteredCount();

		// Initialise page parameters/metadata and validate.
		final BrapiPagedResult<T> pagedResult = new BrapiPagedResult<T>(pgeNum, pgeSize, totalResults, filteredResults);

		// Add list/search result
		pagedResult.addPageResults(searchSpec.getResults(pagedResult));
		return pagedResult;
	}

	public <T> ResponseEntity<List<T>> getPagedResult(final Supplier<Long> countSupplier, final Supplier<List<T>> resultsSupplier,
		final Pageable pageable) {
		final PagedResult<T> resultPage =
			this.execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<T>() {

				@Override
				public long getCount() {
					return countSupplier.get();
				}

				@Override
				public List<T> getResults(final PagedResult<T> pagedResult) {
					return resultsSupplier.get();
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}

	public <T> ResponseEntity<List<T>> getPagedResult(final Supplier<Long> countSupplier, final Supplier<Long> countFilteredSupplier, final Supplier<List<T>> resultsSupplier,
		final Pageable pageable) {
		final PagedResult<T> resultPage =
			this.execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<T>() {

				@Override
				public long getCount() {
					return countSupplier.get();
				}

				@Override
				public long getFilteredCount(){ return countFilteredSupplier.get(); }

				@Override
				public List<T> getResults(final PagedResult<T> pagedResult) {
					return resultsSupplier.get();
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Filtered-Count", Long.toString(resultPage.getFilteredResults()));
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}
}
