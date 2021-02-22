package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.hamcrest.CoreMatchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class BreedingMethodResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	public BreedingMethodService breedingMethodService;

	@Configuration
	public static class TestConfiguration {
		@Bean
		@Primary
		public BreedingMethodService breedingMethodService() {
			return Mockito.mock(BreedingMethodService.class);
		}
	}

	@Before
	public void setup() throws Exception {
		super.setUp();
	}

	@Test
	public void testGetAllBreedingMethods() throws Exception {
		final Pageable pageable = Mockito.mock(Pageable.class);
		Mockito.when(pageable.getPageSize()).thenReturn(20);
		Mockito.when(pageable.getPageNumber()).thenReturn(0);
		Mockito.when(pageable.getSort()).thenReturn(null);
		final List<BreedingMethodDTO> list = new ArrayList<>();
		final BreedingMethodDTO method = new BreedingMethodDTO();
		method.setDescription("Sample Description");
		method.setCode("Sample Code");
		method.setGroup("Sample Group");
		method.setName("Sample Name");
		method.setType("Sample Type");
		method.setMid(1);
		list.add(method);

		Mockito.doReturn(list).when(this.breedingMethodService).getBreedingMethods(Matchers.anyString(), Matchers.any(BreedingMethodSearchRequest.class), Matchers.any(Pageable.class));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/{cropName}/brapi/v2/breedingmethods", this.cropName).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$.result.data[0].abbreviation", CoreMatchers.is(method.getCode())))
			.andExpect(jsonPath("$.result.data[0].breedingMethodDbId", CoreMatchers.is(String.valueOf(method.getMid()))))
			.andExpect(jsonPath("$.result.data[0].breedingMethodName", CoreMatchers.is(method.getName())))
			.andExpect(jsonPath("$.result.data[0].description", CoreMatchers.is(method.getDescription())));


	}

	@Test
	public void testGetBreedingMethodByDbId() throws Exception {
		final Pageable pageable = Mockito.mock(Pageable.class);
		Mockito.when(pageable.getPageSize()).thenReturn(20);
		Mockito.when(pageable.getPageNumber()).thenReturn(0);
		Mockito.when(pageable.getSort()).thenReturn(null);
		final BreedingMethodDTO method = new BreedingMethodDTO();
		method.setDescription("Sample Description");
		method.setCode("Sample Code");
		method.setGroup("Sample Group");
		method.setName("Sample Name");
		method.setType("Sample Type");
		method.setMid(1);

		Mockito.doReturn(method).when(this.breedingMethodService).getBreedingMethod(30);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/{cropName}/brapi/v2/breedingmethods/{breedingMethodDbId}", this.cropName, 30).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$.result.abbreviation", CoreMatchers.is(method.getCode())))
			.andExpect(jsonPath("$.result.breedingMethodDbId", CoreMatchers.is(String.valueOf(method.getMid()))))
			.andExpect(jsonPath("$.result.breedingMethodName", CoreMatchers.is(method.getName())))
			.andExpect(jsonPath("$.result.description", CoreMatchers.is(method.getDescription())));


	}
}
