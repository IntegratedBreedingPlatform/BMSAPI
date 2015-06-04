
package org.ibp.api.rest.common;

import java.util.List;

import org.ibp.api.domain.common.PagedResult;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class PaginatedSearchTest {

	@Test
	public void testExecute() {
		final int pageNumber = 2;
		final int pageSize = 3;
		final List<String> sampleResults = Lists.newArrayList("Item1", "Item2", "Item3", "Item4", "Item5", "Item6");

		PagedResult<String> result = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<String>() {

			@Override
			public List<String> getResults(PagedResult<String> pagedResult) {
				return sampleResults;
			}

			@Override
			public long getCount() {
				return sampleResults.size();
			}
		});

		Assert.assertNotNull(result);
		Assert.assertEquals("Expected number of page results did not match.", sampleResults.size(), result.getPageResults().size());
		Assert.assertEquals("Expected page number to be as requested.", pageNumber, result.getPageNumber());
		Assert.assertEquals("Expected page size to be as requested.", pageSize, result.getPageSize());
	}

	@Test
	public void testExecuteDefaults() {

		final List<String> sampleResults = Lists.newArrayList("Item1", "Item2", "Item3", "Item4", "Item5", "Item6");

		PagedResult<String> result = new PaginatedSearch().execute(null, null, new SearchSpec<String>() {

			@Override
			public List<String> getResults(PagedResult<String> pagedResult) {
				return sampleResults;
			}

			@Override
			public long getCount() {
				return sampleResults.size();
			}
		});

		Assert.assertNotNull(result);
		Assert.assertEquals("Expected number of page results did not match.", sampleResults.size(), result.getPageResults().size());
		Assert.assertEquals("Expected page number to be defaulted when not provided.", PagedResult.DEFAULT_PAGE_NUMBER,
				result.getPageNumber());
		Assert.assertEquals("Expected page size to be defaulted when not provided.", PagedResult.DEFAULT_PAGE_SIZE, result.getPageSize());
	}
}
