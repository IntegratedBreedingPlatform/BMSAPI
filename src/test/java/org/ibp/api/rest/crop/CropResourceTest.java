package org.ibp.api.rest.crop;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

public class CropResourceTest extends ApiUnitTestBase {

	@Autowired
	private CropService cropService;

	@Autowired
	private SecurityService securityService;


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
		final WorkbenchUser workbenchUser = new WorkbenchUser();
		workbenchUser.setUserid(1);

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(workbenchUser);
		Mockito.when(this.cropService.getAvailableCropsForUser(workbenchUser.getUserid())).thenReturn(crops);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crop/list").contentType(this.contentType)) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(crops.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.is("wheat")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1]", Matchers.is("maize")));
	}

}
