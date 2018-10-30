package org.ibp.api.rest.dataset;

import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.dataset.DatasetTrait;
import org.ibp.api.java.dataset.DatasetService;
import org.junit.Before;
import org.junit.Test;
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
	public void testCountPhenotypes() throws Exception {
		final long count = 10;
		doReturn(count).when(this.studyDatasetService).countPhenotypes(100, 102, Arrays.asList(1, 2, 3));
		
		this.mockMvc
			.perform(MockMvcRequestBuilders.head("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables/observations", this.cropName, 100, 102)
					.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}
	
	@Test
	public void testAddDatasetTrait() throws Exception {
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int traitId = random.nextInt(10000);
		final String alias = RandomStringUtils.randomAlphabetic(20);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final DatasetTrait datasetTrait = new DatasetTrait(traitId, alias);
		doReturn(measurementVariable).when(this.studyDatasetService).addDatasetTrait(studyId, datasetId,  datasetTrait);
		
		this.mockMvc
			.perform(MockMvcRequestBuilders.put("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables", this.cropName, studyId, datasetId)
					.contentType(this.contentType)
					.content(this.convertObjectToByte(datasetTrait)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}
}
