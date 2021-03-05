
package org.ibp.api.brapi.v1.calls;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.java.calls.CallService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private CallService callService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public CallService callService() {
			return Mockito.mock(CallService.class);
		}

	}

	private List<Map<String, Object>> calls;

	@Before
	public void setup() {
		final Map<String, Object> map = new HashMap<>();
		map.put("call", "token");
		final List<String> datatypes = new ArrayList<>();
		datatypes.add("json");
		map.put("datatypes", datatypes);

		final List<String> methods = new ArrayList<>();
		methods.add("POST");
		map.put("methods", methods);

		final List<String> versions = new ArrayList<>();
		versions.add("1.0");
		map.put("versions", versions);

		final Map<String, Object> map2 = new HashMap<>();
		map2.put("call", "crops");
		final List<String> datatypes2 = new ArrayList<>();
		datatypes2.add("json");
		map2.put("datatypes", datatypes2);

		final List<String> methods2 = new ArrayList<>();
		methods2.add("GET");
		map2.put("methods", methods2);

		final List<String> versions2 = new ArrayList<>();
		versions2.add("1.0");
		map2.put("versions2", versions2);

		final Map<String, Object> map3 = new HashMap<>();
		map3.put("call", "crops");
		final List<String> datatypes3 = new ArrayList<>();
		datatypes3.add("json,csv,tsv");
		map3.put("datatypes", datatypes3);

		final List<String> methods3 = new ArrayList<>();
		methods3.add("GET");
		map3.put("methods", methods3);

		final List<String> versions3 = new ArrayList<>();
		versions3.add("1.0");
		map3.put("versions2", versions3);

		this.calls = Arrays.asList(map, map2, map3);
	}

	@Test
	public void testListAvailableCalls() throws Exception {
		// TODO user test/resources/brapi/calls.json
		Mockito.when(this.callService.getAllCalls(null, CallResourceBrapi.VERSION_1, null, null)).thenReturn(this.calls);
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/calls").build();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(this.calls.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0]", Matchers.is(this.calls.get(0))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1]", Matchers.is(this.calls.get(1))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2]", Matchers.is(this.calls.get(2))))
			// Default starting page index is 0
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage",
				Matchers.is(BrapiPagedResult.DEFAULT_PAGE_NUMBER)))
			// Default page size is 1000
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(BrapiPagedResult.DEFAULT_PAGE_SIZE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(3))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))); //
	}

	@Test
	public void testListAvailableCallsWithDatatype() throws Exception {
		Mockito.when(this.callService.getAllCalls("csv", CallResourceBrapi.VERSION_1, null, null)).thenReturn(this.calls);
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/calls").queryParam("datatype", "csv").build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(this.calls.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0]", Matchers.is(this.calls.get(0))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1]", Matchers.is(this.calls.get(1))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2]", Matchers.is(this.calls.get(2))))
			// Default starting page index is 0
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage",
				Matchers.is(BrapiPagedResult.DEFAULT_PAGE_NUMBER)))
			// Default page size is 1000
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(BrapiPagedResult.DEFAULT_PAGE_SIZE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(3))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))); //
	}
}
