package org.ibp.api.domain.common;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;


public class PagedResultTest {

	@Test(expected = IllegalArgumentException.class)
	public void testInitPageSizeLessThanMin() {
		new PagedResult<>(2, 0, 200);
		Assert.fail("Expected validation failure when page size is less than 1.");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitPageSizeMoreThanMax() {
		new PagedResult<>(2, PagedResult.MAX_PAGE_SIZE + 1, 200);
		Assert.fail("Expected validation failure when page size is more tha max allowed which is " + PagedResult.MAX_PAGE_SIZE);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitPageNumberLessThanMin() {
		new PagedResult<>(0, PagedResult.MAX_PAGE_SIZE, 200);
		Assert.fail("Expected validation failure when page number is less than 1.");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitPageNumberMoreLastPage() {
		new PagedResult<>(10, PagedResult.MAX_PAGE_SIZE, 200);
		Assert.fail("Expected validation failure when page number is more than total number of pages available.");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitValidParametersInvalidResults() {
		PagedResult<String> pagedResult = new PagedResult<>(2, 10, 200);
		Assert.assertNotNull("Exected PagedResult object initialized as the parameters supplied are valid.", pagedResult);
		pagedResult.addPageResults(null);
	}

	@Test
	public void testInitValidParameters() {
		PagedResult<String> pagedResult = new PagedResult<>(2, 10, 200);
		Assert.assertNotNull("Exected PagedResult object initialized as the parameters supplied are valid.", pagedResult);
		pagedResult.addPageResults(Lists.newArrayList("one", "two"));
		Assert.assertTrue("Expected PagedResult.pageResults collection initialized as the parameters supplied are valid.", !pagedResult.getPageResults().isEmpty());
	}
	
	@Test
	public void testFirstPage() {
		PagedResult<String> pagedResult = new PagedResult<>(1, 10, 200);
		Assert.assertTrue("Expected to be on the first page.", pagedResult.isFirstPage());
		Assert.assertFalse("Expected to not have a previous page because I am on first page.", pagedResult.isHasPreviousPage());
		Assert.assertTrue("Expected to have a next page because I am on first page and there are more pages after me.", pagedResult.isHasNextPage());
	}
	
	@Test
	public void testLastPage() {
		PagedResult<String> pagedResult = new PagedResult<>(20, 10, 200);
		Assert.assertTrue("Expected to be on the last page.", pagedResult.isLastPage());
		Assert.assertFalse("Expected to not have a next page because I am on last page.", pagedResult.isHasNextPage());
		Assert.assertTrue("Expected to have a previous page because I am on last page and there are more pages before me.", pagedResult.isHasPreviousPage());
	}
	
	@Test
	public void testSomePageOtherThanFirstOrLast() {
		PagedResult<String> pagedResult = new PagedResult<>(10, 10, 200);
		Assert.assertTrue("Expected to have a next page because I am on some page other than first or last.", pagedResult.isHasNextPage());
		Assert.assertTrue("Expected to have a previous page because I am on some page other than first or last.", pagedResult.isHasPreviousPage());
	}
	
	/**
	 * Case when first page with a valid page size is requested but the actural search/listing returns no actual matching results. 
	 */
	@Test
	public void testZeroTotalResults() {
		PagedResult<String> pagedResult = new PagedResult<>(1, 20, 0);
		Assert.assertNotNull("Exected PagedResult object initialized as the parameters supplied are valid.", pagedResult);
	}
}
