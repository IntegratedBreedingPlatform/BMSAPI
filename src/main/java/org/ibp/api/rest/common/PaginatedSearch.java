
package org.ibp.api.rest.common;

import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.domain.common.PagedResult;

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

		// Initialise page parameters/metadata and validate.
		final PagedResult<T> pagedResult = new PagedResult<T>(pgeNum, pgeSize, totalResults);

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

		// Initialise page parameters/metadata and validate.
		final BrapiPagedResult<T> pagedResult = new BrapiPagedResult<T>(pgeNum, pgeSize, totalResults);

		// Add list/search result
		pagedResult.addPageResults(searchSpec.getResults(pagedResult));
		return pagedResult;
	}
}
