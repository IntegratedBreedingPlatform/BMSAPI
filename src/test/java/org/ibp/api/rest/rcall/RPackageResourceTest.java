package org.ibp.api.rest.rcall;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.rpackage.RCallDTO;
import org.ibp.api.java.rpackage.RPackageService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class RPackageResourceTest extends ApiUnitTestBase {

	@Autowired
	private RPackageService rPackageService;

	@Test
	public void testGetRCallsByPackageId() throws Exception {

		final int packageId = new Random().nextInt();
		final RCallDTO rCallDTO = this.createTesttRCallDTO();

		when(this.rPackageService.getRCallsByPackageId(packageId)).thenReturn(Arrays.asList(rCallDTO));

		this.mockMvc.perform(MockMvcRequestBuilders
				.get("/r-packages/{packageId}/r-calls", packageId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$[0].rCallId", Matchers.is(rCallDTO.getrCallId())))
			.andExpect(jsonPath("$[0].description", Matchers.is(rCallDTO.getDescription())))
			.andExpect(jsonPath("$[0].endpoint", Matchers.is(rCallDTO.getEndpoint())))
			.andExpect(jsonPath("$[0].aggregate", Matchers.is(rCallDTO.isAggregate())))
			.andExpect(jsonPath("$[0].parameters." + rCallDTO.getParameters().entrySet().iterator().next().getKey(),
				Matchers.is(rCallDTO.getParameters().entrySet().iterator().next().getValue())));
	}

	private RCallDTO createTesttRCallDTO() {
		final RCallDTO rCallDTO = new RCallDTO();
		rCallDTO.setDescription(RandomStringUtils.randomAlphanumeric(10));
		rCallDTO.setEndpoint(RandomStringUtils.randomAlphanumeric(10));
		rCallDTO.setAggregate(true);
		rCallDTO.setrCallId(1);
		final Map<String, String> parameters = new HashMap<>();
		parameters.put(RandomStringUtils.randomAlphanumeric(10), RandomStringUtils.randomAlphanumeric(10));
		rCallDTO.setParameters(parameters);
		return rCallDTO;
	}

}
