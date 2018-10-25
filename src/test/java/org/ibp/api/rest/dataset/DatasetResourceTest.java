package org.ibp.api.rest.dataset;

import static org.mockito.Mockito.doReturn;

import java.util.Arrays;

import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.study.StudyDatasetService;
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
	private StudyDatasetService studyDatasetService;
	
	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public StudyDatasetService studyDatasetService() {
			return Mockito.mock(StudyDatasetService.class);
		}
	}

	@Before
	public void setup() throws Exception {
		super.setUp();
	}
	
	@Test
	public void testCountPhenotypesForInvalidDataset() throws Exception {
		doReturn(false).when(this.studyDatasetService).datasetExists(Matchers.anyInt(), Matchers.anyInt());
		
		this.mockMvc
			.perform(MockMvcRequestBuilders.head("/crops/{crop}/{studyId}/datasets/{datasetId}/variables/observations", this.cropName, 100, 102)
					.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void testCountPhenotypesForValidDataset() throws Exception {
		doReturn(true).when(this.studyDatasetService).datasetExists(Matchers.anyInt(), Matchers.anyInt());
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
