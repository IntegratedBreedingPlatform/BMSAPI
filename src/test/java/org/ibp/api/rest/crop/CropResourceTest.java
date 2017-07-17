package org.ibp.api.rest.crop;

import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.crop.CropService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class CropResourceTest extends ApiUnitTestBase {

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
	public void listAvailableCrops() throws Exception {

		final List<String> crops = Arrays.asList("wheat", "maize");

		Mockito.when(cropService.getInstalledCrops()).thenReturn(crops);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crop/list").contentType(this.contentType)) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(crops.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.is("wheat")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]", Matchers.is("maize")));
	}

}
