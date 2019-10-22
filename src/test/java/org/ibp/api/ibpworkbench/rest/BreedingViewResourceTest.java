package org.ibp.api.ibpworkbench.rest;

import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.design.DesignLicenseService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class BreedingViewResourceTest extends ApiUnitTestBase {

	@Autowired
	private DesignLicenseService designLicenseService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public DesignLicenseService designLicenseService() {
			return Mockito.mock(DesignLicenseService.class);
		}
	}

	@Test
	public void testCountExpiryDays() throws Exception {
		final int expiryDays = 101;
		Mockito.when(this.designLicenseService.getExpiryDays()).thenReturn(expiryDays);

		this.mockMvc
			.perform(MockMvcRequestBuilders.head("/breeding_view/license/expiryDays")
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(expiryDays)));
	}




}
