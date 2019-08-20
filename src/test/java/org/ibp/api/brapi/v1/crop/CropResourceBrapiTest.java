
package org.ibp.api.brapi.v1.crop;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.crop.CropService;
import org.junit.Ignore;
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

import java.util.Arrays;
import java.util.List;

public class CropResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private CropService cropService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public CropService cropService() {
			return Mockito.mock(CropService.class);
		}

	}

	@Test @Ignore // FIXME
	public void testListAvailableCrops() throws Exception {

		final List<String> crops = Arrays.asList("Maize", "Wheat", "Cowpea", "pearlmillet");
		Mockito.when(this.cropService.getInstalledCrops()).thenReturn(crops);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/brapi/v1/crops").build();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(4)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0]", Matchers.is(crops.get(0))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1]", Matchers.is(crops.get(1))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2]", Matchers.is(crops.get(2))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[3]", Matchers.is(crops.get(3))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage", Matchers.is(0)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(0)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(0)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(0)));

	}

}
