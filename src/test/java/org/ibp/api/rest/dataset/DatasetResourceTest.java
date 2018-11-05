package org.ibp.api.rest.dataset;

import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsMapContainingKey;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.trial.TrialResourceBrapiTest;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.exception.ResourceNotFoundException;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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
			.perform(MockMvcRequestBuilders.put("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables", this.cropName, studyId, datasetId)
					.contentType(this.contentType)
					.content(this.convertObjectToByte(datasetVariable)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testGetDatasets() throws Exception {
		final List<DatasetDTO> datasets= createDatasets(Arrays.asList(10090, 10094, 10095));
		doReturn(datasets).when(this.studyDatasetService).getDatasets(100, null);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100, null)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(datasets.size())));
	}

	@Test
	public void testGetDatasetsWithFilter() throws Exception {
		final List<DatasetDTO> datasets= createDatasets(Arrays.asList(10090));
		doReturn(datasets).when(this.studyDatasetService).getDatasets(100, null);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100, null)
				.param("filterByTypeIds", "10090").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(datasets.size())));
	}


	@Test
	public void testGetDatasetsErrorStudyIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		errors.reject("study.not.exist", "");
		Mockito.when(this.studyDatasetService.getDatasets(100, null)).thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100, null)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("Study does not exist")));

	}

	@Test
	public void testGetDatasetsErrorDatasetTypeIdIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Set<Integer> filterByTypeIds = new TreeSet<>();
		filterByTypeIds.add(100);
		errors.reject("dataset.type.id.not.exist", new Object[] {100}, "");
		Mockito.when(this.studyDatasetService.getDatasets(100,filterByTypeIds))
				.thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100, null)
				.param("filterByTypeIds", "100").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("100 is not a valid dataset type")));
	}

	private static List<DatasetDTO> createDatasets(final List<Integer> datasetTypes) {
		final List<DatasetDTO> datasets = new ArrayList<>();
		int num = datasetTypes.size();
		for (final Integer datasetType : datasetTypes) {
			final DataSetType dataSetType = DataSetType.findById(datasetType);
			final DatasetDTO datasetDTO = new DatasetDTO();
			datasetDTO.setDatasetTypeId(dataSetType.getId());
			datasetDTO.setName(dataSetType.name() + "_" + num);
			num--;
		}
		return datasets;
	}

	@Test
	public void testGetObservations() throws Exception {
		final ObservationUnitData measurement =
			new ObservationUnitData(12, 1, "123", Phenotype.ValueStatus.OUT_OF_SYNC);
		final ObservationUnitRow obsDto = new ObservationUnitRow();

		obsDto.setObservationUnitId(11);
		obsDto.setGid(22);
		obsDto.setDesignation("CHMEPwuxU2Yr6");
		obsDto.setAction("11");
		final Map<String, ObservationUnitData> map = new HashMap<>();
		map.put("TEST1", measurement);
		obsDto.setVariables(map);

		Mockito.when(this.studyDatasetService.countTotalObservationUnitsForDataset(
			org.mockito.Matchers.anyInt(),
			org.mockito.Matchers.anyInt()))
			.thenReturn(100);
		Mockito.when(this.studyDatasetService.getObservationUnitRows(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(),
			org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyString(),
			org.mockito.Matchers.anyString())).thenReturn(Lists.newArrayList(obsDto));
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int instanceId = random.nextInt(10000);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get(
				"/crops/{cropname}/studies/{studyId}/datasets/{datasetId}/instances/{instanceId}/observationUnits/table",
				this.cropName,
				studyId,
				datasetId,
				instanceId).param("pageNumber", "1")
				.param("pageSize", "100").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.recordsFiltered", Matchers.is(100)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.recordsTotal", Matchers.is(100)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.draw", Matchers.is("1")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.data[0].observationUnitId", Matchers.is(obsDto.getObservationUnitId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.data[0].gid", Matchers.is(obsDto.getGid())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.data[0].designation", Matchers.is(obsDto.getDesignation())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.data[0].action", Matchers.is(obsDto.getAction())))
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.data[0].variables[\"TEST1\"].observationId",
					Matchers.is(measurement.getObservationId())))
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.data[0].variables[\"TEST1\"].categoricalValueId",
					Matchers.is(measurement.getCategoricalValueId())))
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.data[0].variables[\"TEST1\"].value",
					Matchers.is(measurement.getValue())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.data[0].variables[\"TEST1\"].status",
				Matchers.is(measurement.getStatus().getName())))
		;
	}

}
