
package org.ibp.api.domain.common;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic container for paginated results with common paging metadata and validation.
 *
 * <p>
 * Page numbers start from 1 up to the total number of pages depending on total number of results found.
 * <p>
 * Minimum page size enforced is 1 and maximum is MAX_PAGE_SIZE. Max page size is enforces so that large page sizes don't end up making
 * pagination effectively like no pagination!
 *
 * @param <T> the type of list the page contains.
 */
@Component
public class PagedResult<T> {

	private final List<T> pageResults = new ArrayList<T>();
	protected int pageNumber;
	protected int pageSize;
	protected long totalResults;
	protected long filteredResults;
	private String sortBy;
	private String sortOrder;

	public static int MAX_PAGE_SIZE;
	public static final int DEFAULT_PAGE_SIZE = 1000;
	public static final int DEFAULT_PAGE_NUMBER = 0;

	public static final String CURRENT_PAGE_DESCRIPTION = "Page number to retrieve in case of multi paged results. Defaults to "
			+ PagedResult.DEFAULT_PAGE_NUMBER + " (first page) if not supplied.";
	public static final String PAGE_SIZE_DESCRIPTION = "Number of results to retrieve per page. Defaults to "
			+ PagedResult.DEFAULT_PAGE_SIZE + " if not supplied.";

	protected PagedResult() {
		// Empty constructor needed for subclass constructor
	}

	public PagedResult(final int pageNumber, final int pageSize, final long totalResults, final long filteredResults) {
		this.totalResults = totalResults;
		this.filteredResults = filteredResults;

		if (pageSize < 1 || pageSize > PagedResult.MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("Page size must between 0 and " + PagedResult.MAX_PAGE_SIZE + ".");
		}
		this.pageSize = pageSize;

		if (totalResults != 0 && (pageNumber < PagedResult.DEFAULT_PAGE_NUMBER || pageNumber > this.getTotalPages())) {
			throw new IllegalArgumentException(
					"A total of " + this.getTotalPages() + " pages are available, so the page number must between "
							+ PagedResult.DEFAULT_PAGE_NUMBER + " and " + this.getTotalPages() + ".");
		}

		this.pageNumber = pageNumber;
	}

	public List<T> getPageResults() {
		return this.pageResults;
	}

	public void addPageResults(final List<T> pageResults) {
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

	public long getFilteredResults() {
		return this.filteredResults;
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

	@Value("${pagedresult.max.page.size}")
	public void setMaxPageSize(final String maxPageSize) {
		if(maxPageSize!=null){
			MAX_PAGE_SIZE = Integer.parseInt(maxPageSize);
		}
	}

}
