
package org.ibp.api.rest.common;

import org.ibp.api.domain.common.PagedResult;

/**
 * Contains the general pattern for executing paginated searches.
 *
 * @author Naymesh Mistry
 */
public class PaginatedSearch {

	public <T> PagedResult<T> execute(Integer pageNumber, Integer pageSize, SearchSpec<T> searchSpec) {

		// Default page parameters if not supplied.
		if (pageNumber == null) {
			pageNumber = new Integer(PagedResult.DEFAULT_PAGE_NUMBER);
		}

		if (pageSize == null) {
			pageSize = new Integer(PagedResult.DEFAULT_PAGE_SIZE);
		}

		long totalResults = searchSpec.getCount();

		// Initialise page parameters/metadata and validate.
		PagedResult<T> pagedResult = new PagedResult<T>(pageNumber, pageSize, totalResults);

		// Add list/search result
		pagedResult.addPageResults(searchSpec.getResults(pagedResult));
		return pagedResult;
	}
}
