
package org.ibp.api.domain.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic container for paginated results with common paging metadata and validation.
 * 
 * <p>Page numbers start from 1 upto the total number of pages depending on total number of results found.
 * <p>Minimum page size enforced is 1 and maximum is MAX_PAGE_SIZE. Max page size is enforces so that large page sizes don't end up making pagination effectively like no pagination!
 *
 * @param <T> the type of list the page contains.
 */
public class PagedResult<T> {

	private final List<T> pageResults = new ArrayList<T>();
	private final int pageNumber;
	private final int pageSize;
	private final long totalResults;
	
	public static int MAX_PAGE_SIZE = 200;

	public PagedResult(int pageNumber, int pageSize, long totalResults) {
		this.totalResults = totalResults;

		if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("Page size must between 1 and "+ MAX_PAGE_SIZE + ".");
		}
		this.pageSize = pageSize;
		
		if (totalResults != 0) {
			if (pageNumber < 1 || pageNumber > getTotalPages()) {
				throw new IllegalArgumentException("A total of " + getTotalPages() + " pages are available, so the page number must between 1 and " + getTotalPages() + ".");
			}
		}
		this.pageNumber = pageNumber;
	}

	public List<T> getPageResults() {
		return this.pageResults;
	}
	
	public void addPageResults(List<T> pageResults) {
		if (pageResults == null) {
			throw new IllegalArgumentException("Page results must not be null.");
		}
		this.pageResults.addAll(pageResults);
	}

	public int getPageNumber() {
		return this.pageNumber;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public long getTotalResults() {
		return this.totalResults;
	}

	public int getTotalPages() {
		return (int) Math.ceil((double) getTotalResults() / (double) getPageSize());
	}

	public boolean isFirstPage() {
		return getPageNumber() == 1;
	}

	public boolean isLastPage() {
		return getPageNumber() == getTotalPages();
	}

	public boolean isHasNextPage() {
		return !isLastPage();
	}

	public boolean isHasPreviousPage() {
		return !isFirstPage();
	}

}
