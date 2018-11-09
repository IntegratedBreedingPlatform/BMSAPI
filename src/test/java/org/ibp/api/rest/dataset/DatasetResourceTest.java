package org.ibp.api.rest.dataset;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.ObservationValue;
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

import java.util.Arrays;
import java.util.Random;

import static org.mockito.Mockito.doReturn;

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
			.perform(MockMvcRequestBuilders
				.head("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables/observations", this.cropName, 100, 102)
				.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}

	@Test
	public void testCountPhenotypesByObservation() throws Exception {
		final long count = 11;
		doReturn(count).when(this.studyDatasetService).countPhenotypesByInstance(100, 102, 103);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.head("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{instanceId}", this.cropName, 100, 102, 103)
				.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}

	@Test
	public void testAddDatasetVariable() throws Exception {
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int traitId = random.nextInt(10000);
		final String alias = RandomStringUtils.randomAlphabetic(20);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), traitId, alias);
		doReturn(measurementVariable).when(this.studyDatasetService).addDatasetVariable(studyId, datasetId, datasetVariable);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.put("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables", this.cropName, studyId, datasetId)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(datasetVariable)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testRemoveVariables() throws Exception {
		final int studyId = 100;
		final int datasetId = 102;
		this.mockMvc
			.perform(MockMvcRequestBuilders
				.delete("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables", this.cropName, studyId, datasetId)
				.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
		Mockito.verify(this.studyDatasetService).removeVariables(studyId, datasetId, Arrays.asList(1, 2, 3));
	}

	@Test
	public void testAddObservation() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int observationUnitId = random.nextInt(10000);
		final ObservationDto observationDto = new ObservationDto();

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}", this.cropName, studyId,
					datasetId, observationUnitId)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(observationDto)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.studyDatasetService)
			.addObservation(Matchers.eq(studyId), Matchers.eq(datasetId), Matchers.eq(observationUnitId),
				Matchers.any(ObservationDto.class));
	}

	@Test
	public void testUpdateObservation() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int observationUnitId = random.nextInt(10000);
		final int observationId = random.nextInt(10000);
		final ObservationValue observationValue = new ObservationValue();

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.patch(
					"/crops/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}",
					this.cropName, studyId, datasetId, observationUnitId, observationId)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(observationValue)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.studyDatasetService)
			.updateObservation(Matchers.eq(studyId), Matchers.eq(datasetId), Matchers.eq(observationId), Matchers.eq(observationUnitId),
				Matchers.any(ObservationValue.class));
	}
}
