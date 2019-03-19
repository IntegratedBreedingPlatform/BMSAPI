
package org.ibp.api.rest.common;

import java.util.List;

import org.ibp.api.domain.common.PagedResult;

/**
 * The call back interface for {@link PaginatedSearch}es.
 *
 * @author Naymesh Mistry
 *
 * @param <T> the entity being searched/listed.
 */
public interface SearchSpec<T> {

	long getCount();

	default long getFilteredCount() {
		return 0;
	}

	List<T> getResults(PagedResult<T> pagedResult);
}
