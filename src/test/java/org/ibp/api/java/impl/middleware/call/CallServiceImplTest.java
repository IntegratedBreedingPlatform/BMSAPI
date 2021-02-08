package org.ibp.api.java.impl.middleware.call;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class CallServiceImplTest {

	@Mock
	private Resource calls;

	@InjectMocks
	private CallServiceImpl callService;

	@Before
	public void setup() throws IOException {
		this.resetInputStream();
	}

	@Test
	public void testGetAllCalls() throws IOException {
		final int totalCalls = 27;

		final List<Map<String, Object>> result = this.callService.getAllCalls(null, 10, 0);
		Assert.assertEquals("First page should contain 10 records", 10, result.size());
		this.resetInputStream();

		final List<Map<String, Object>> result2 = this.callService.getAllCalls(null, 10, 1);
		Assert.assertEquals("Second page should contain 10 records", 10, result2.size());
		this.resetInputStream();

		final List<Map<String, Object>> result3 = this.callService.getAllCalls(null, null, null);
		Assert.assertEquals("Should return all records if pageSize and pageNumber are not specified", totalCalls, result3.size());
		this.resetInputStream();

		// Search by BrAPI v1.2 where CSV data type = csv
		final List<Map<String, Object>> result4 = this.callService.getAllCalls("csv", 10, 0);
		Assert.assertEquals("There is only one call service with csv datatype", 1, result4.size());
		this.resetInputStream();

		// Search by BrAPI v1.2 where CSV data type = text/csv
		final List<Map<String, Object>> result5 = this.callService.getAllCalls("text/csv", 10, 0);
		Assert.assertEquals("There is only one call service with text/csv datatype", 1, result5.size());
		this.resetInputStream();

		final List<Map<String, Object>> result6 = this.callService.getAllCalls(null, 5, null);
		Assert.assertEquals("Should return no. of records specified even if pageNumber is not specified", 5, result6.size());
		this.resetInputStream();

		final List<Map<String, Object>> result7 = this.callService.getAllCalls(null, null, 0);
		Assert.assertEquals("Should return all records if pageSize is specified and pageNumber is zero", totalCalls, result7.size());
		this.resetInputStream();

	}

	private void resetInputStream() throws IOException {
		final InputStream is = new FileInputStream(new File("src/test/resources/brapi/calls.json"));
		doReturn(is).when(this.calls).getInputStream();
	}

}
