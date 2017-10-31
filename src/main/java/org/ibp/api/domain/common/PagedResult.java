
package org.ibp.api.domain.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic container for paginated results with common paging metadata and validation.
 *
 * <p>
 * Page numbers start from 1 upto the total number of pages depending on total number of results found.
 * <p>
 * Minimum page size enforced is 1 and maximum is MAX_PAGE_SIZE. Max page size is enforces so that large page sizes don't end up making
 * pagination effectively like no pagination!
 *
 * @param <T> the type of list the page contains.
 */
public class PagedResult<T> {

	private final List<T> pageResults = new ArrayList<T>();
	private final int pageNumber;
	private final int pageSize;
	private final long totalResults;
	private String sortBy;
	private String sortOrder;

	public static final int MAX_PAGE_SIZE = 200;
	public static final int DEFAULT_PAGE_SIZE = 100;
	public static final int DEFAULT_PAGE_NUMBER = 1;
	
	public static final String CURRENT_PAGE_DESCRIPTION =
			"Page number to retrieve in case of multi paged results. Defaults to " + DEFAULT_PAGE_NUMBER + " (first page) if not supplied.";
	public static final String PAGE_SIZE_DESCRIPTION =
			"Number of results to retrieve per page. Defaults to " + DEFAULT_PAGE_SIZE + " if not supplied. Max page size allowed is "
					+ MAX_PAGE_SIZE + ".";

	public PagedResult(int pageNumber, int pageSize, long totalResults) {
		this.totalResults = totalResults;

		if (pageSize < 1 || pageSize > PagedResult.MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("Page size must between 1 and " + PagedResult.MAX_PAGE_SIZE + ".");
		}
		this.pageSize = pageSize;

		if ((totalResults != 0) && (pageNumber < 1 || pageNumber > this.getTotalPages())) {
			throw new IllegalArgumentException("A total of " + this.getTotalPages()
					+ " pages are available, so the page number must between 1 and " + this.getTotalPages() + ".");
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

	public String getSortBy() {
		return this.sortBy;
	}

	public void setSortBy(final String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortOrder() {
		return this.sortOrder;
	}

	public void setSortOrder(final String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public long getTotalResults() {
		return this.totalResults;
	}

	public int getTotalPages() {
		return (int) Math.ceil((double) this.getTotalResults() / (double) this.getPageSize());
	}

	public boolean isFirstPage() {
		return this.getPageNumber() == 1;
	}

	public boolean isLastPage() {
		return this.getPageNumber() == this.getTotalPages();
	}

	public boolean isHasNextPage() {
		return !this.isLastPage();
	}

	public boolean isHasPreviousPage() {
		return !this.isFirstPage();
	}

}
