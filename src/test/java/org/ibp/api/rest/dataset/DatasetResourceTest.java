package org.ibp.api.rest.dataset;

import static org.mockito.Mockito.doReturn;

import java.util.Arrays;

import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.dataset.DatasetService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


public class DatasetResourceTest extends ApiUnitTestBase {
	
	@Autowired
	private DatasetService studyDatasetService;
	
	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public DatasetService studyDatasetService() {
			return Mockito.mock(DatasetService.class);
		}
	}

	@Before
	public void setup() throws Exception {
		super.setUp();
	}
	
	@Test
	public void testCountPhenotypesForInvalidDataset() throws Exception {
		this.mockMvc
			.perform(MockMvcRequestBuilders.head("/crops/{crop}/{studyId}/datasets/{datasetId}/variables/observations", this.cropName, 100, 102)
					.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void testCountPhenotypesForValidDataset() throws Exception {
		final long count = 10;
		doReturn(count).when(this.studyDatasetService).countPhenotypesForDataset(102, Arrays.asList(1, 2, 3));
		
		this.mockMvc
			.perform(MockMvcRequestBuilders.head("/crops/{crop}/{studyId}/datasets/{datasetId}/variables/observations", this.cropName, 100, 102)
					.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}
}
