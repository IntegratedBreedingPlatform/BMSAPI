package org.ibp.api.java.impl.middleware.call;

import org.ibp.api.java.calls.CallService;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class CallServiceImplTest {

	private final CallService callService = new CallServiceImpl();

	@Test
	public void testGetAllCalls() {

		final List<Map<String, Object>> result = this.callService.getAllCalls(null, 10, 0);
		Assert.assertEquals("First page should contain 10 records",10, result.size());

		final List<Map<String, Object>> result2 = this.callService.getAllCalls(null, 10, 1);
		Assert.assertEquals("Second page should contain 9 records", 9, result2.size());

		final List<Map<String, Object>> result3 = this.callService.getAllCalls(null, null, null);
		Assert.assertEquals("Should return all records if pageSize and pageNumber are not specified", 19, result3.size());

		// Search by BrAPI v1.2 where CSV data type = csv
		final List<Map<String, Object>> result4 = this.callService.getAllCalls("csv", 10, 0);
		Assert.assertEquals("There is only one call service with csv datatype", 1, result4.size());

		// Search by BrAPI v1.2 where CSV data type = text/csv
		final List<Map<String, Object>> result5 = this.callService.getAllCalls("text/csv", 10, 0);
		Assert.assertEquals("There is only one call service with text/csv datatype", 1, result5.size());

	}

}
