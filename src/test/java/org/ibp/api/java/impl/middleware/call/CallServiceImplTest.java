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
	private Resource callsV1;

	@Mock
	private Resource callsV2;

	@InjectMocks
	private CallServiceImpl callService;

	@Test
	public void testGetAllCallsForV1() throws IOException {
		this.resetInputStreamForV1();
		final List<Map<String, Object>> result = this.callService.getAllCallsForV1(null, 10, 0);
		Assert.assertEquals("First page should contain 10 records", 10, result.size());
		this.resetInputStreamForV1();

		final List<Map<String, Object>> result2 = this.callService.getAllCallsForV1(null, 10, 1);
		Assert.assertEquals("Second page should contain 10 records", 10, result2.size());
		this.resetInputStreamForV1();

		final List<Map<String, Object>> result3 = this.callService.getAllCallsForV1(null, null, null);
		Assert.assertEquals("Should return all records if pageSize and pageNumber are not specified", 20, result3.size());
		this.resetInputStreamForV1();

		// Search by BrAPI v1.2 where CSV data type = csv
		final List<Map<String, Object>> result4 = this.callService.getAllCallsForV1("csv", 10, 0);
		Assert.assertEquals("There is only one call service with csv datatype", 1, result4.size());
		this.resetInputStreamForV1();

		// Search by BrAPI v1.2 where CSV data type = text/csv
		final List<Map<String, Object>> result5 = this.callService.getAllCallsForV1("text/csv",10, 0);
		Assert.assertEquals("There is only one call service with text/csv datatype", 1, result5.size());
		this.resetInputStreamForV1();

		final List<Map<String, Object>> result6 = this.callService.getAllCallsForV1(null,5, null);
		Assert.assertEquals("Should return no. of records specified even if pageNumber is not specified", 5, result6.size());
		this.resetInputStreamForV1();

		final List<Map<String, Object>> result7 = this.callService.getAllCallsForV1(null, null, 0);
		Assert.assertEquals("Should return all records if pageSize is specified and pageNumber is zero", 20, result7.size());
	}

	@Test
	public void testGetAllCallsForV2() throws IOException {
		this.resetInputStreamForV2();
		List<Map<String, Object>> result = this.callService.getAllCallsForV2(null);
		Assert.assertEquals("First page should contain 10 records", 12, result.size());

		this.resetInputStreamForV2();
		result = this.callService.getAllCallsForV2("application/json");
		Assert.assertEquals("Should return 12 records", 12, result.size());

	}

	private void resetInputStreamForV1() throws IOException {
		final InputStream is = new FileInputStream(new File("src/test/resources/brapi/calls_v1.json"));
		doReturn(is).when(this.callsV1).getInputStream();
	}

	private void resetInputStreamForV2() throws IOException {
		final InputStream is = new FileInputStream(new File("src/test/resources/brapi/calls_v2.json"));
		doReturn(is).when(this.callsV2).getInputStream();
	}

}
