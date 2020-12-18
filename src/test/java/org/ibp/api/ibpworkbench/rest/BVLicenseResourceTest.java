package org.ibp.api.ibpworkbench.rest;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.design.License;
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

public class BVLicenseResourceTest extends ApiUnitTestBase {

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
		final License license = new License("Successful license checkout", "73", "31-DEC-2030");
		Mockito.when(this.designLicenseService.getLicenseInfo()).thenReturn(license);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/breeding-view-licenses")
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is(license.getMessage())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].expiry", Matchers.is(license.getExpiry())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].expiryDays", Matchers.is(license.getExpiryDays())))
			.andDo(MockMvcResultHandlers.print());
	}




}
