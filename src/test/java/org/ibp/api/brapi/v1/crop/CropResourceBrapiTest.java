package org.ibp.api.brapi.v1.crop;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.crop.CropService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.ListUtil;
import org.mockito.runners.MockitoJUnitRunner;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

	@Test
	public void testListAvailableCrops() throws Exception {

		final List<String> crops = Arrays.asList("Maize", "Wheat", "Cowpea", "pearlmillet");

		Mockito.when(cropService.getInstalledCrops()).thenReturn(crops);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/brapi/v1/crops").build();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(4)))
				.andExpect(jsonPath("$.result.data[0]", is(crops.get(0))))
				.andExpect(jsonPath("$.result.data[1]", is(crops.get(1))))
				.andExpect(jsonPath("$.result.data[2]", is(crops.get(2))))
				.andExpect(jsonPath("$.result.data[3]", is(crops.get(3))))
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", is(0)))
				.andExpect(jsonPath("$.metadata.pagination.pageSize", is(0)))
				.andExpect(jsonPath("$.metadata.pagination.totalCount", is(0)))
				.andExpect(jsonPath("$.metadata.pagination.totalPages", is(0)));

	}

}
