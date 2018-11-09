package org.ibp.api.rest.dataset;

import static org.hamcrest.core.Is.is;
import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.ibp.api.domain.study.StudyInstance;

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
			.perform(MockMvcRequestBuilders.put("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables", this.cropName, studyId, datasetId)
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
			.perform(MockMvcRequestBuilders.delete("/crops/{crop}/studies/{studyId}/datasets/{datasetId}/variables", this.cropName, studyId, datasetId)
					.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
		Mockito.verify(this.studyDatasetService).removeVariables(studyId, datasetId, Arrays.asList(1, 2, 3));
	}

	@Test
	public void testGetDatasets() throws Exception {
		final List<DatasetDTO> datasets= createDatasets(Arrays.asList(10090, 10094, 10095));
		doReturn(datasets).when(this.studyDatasetService).getDatasets(100, null);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(datasets.size())));
	}

	@Test
	public void testGetDatasetsWithFilter() throws Exception {
		final List<DatasetDTO> datasets = createDatasets(Arrays.asList(10090));
		doReturn(datasets).when(this.studyDatasetService).getDatasets(100, null);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100)
				.param("datasetTypeIds", "10090").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(datasets.size())));
	}


	@Test
	public void testGetDatasetsErrorStudyIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		errors.reject("study.not.exist", "");
		Mockito.when(this.studyDatasetService.getDatasets(100, null)).thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("Study does not exist")));

	}

	@Test
	public void testGetDatasetsErrorDatasetTypeIdIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Set<Integer> datasetTypeIds = new TreeSet<>();
		datasetTypeIds.add(100);
		errors.reject("dataset.type.id.not.exist", new Object[] {100}, "");
		Mockito.when(this.studyDatasetService.getDatasets(100, datasetTypeIds)).thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets", this.cropName, 100)
			.param("datasetTypeIds", "100").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("100 is not a valid dataset type")));
	}

	@Test
	public void testGetDataset() throws Exception {
		final DatasetDTO dataset = createDataset(10090,101101,"Plant",this.cropName,100);
		doReturn(dataset).when(this.studyDatasetService).getDataset(this.cropName, 100,101);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets/{datasetId}", this.cropName, 100,101)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.datasetId", is(dataset.getDatasetId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.name", is(dataset.getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.cropName", is(dataset.getCropName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.datasetTypeId", is(dataset.getDatasetTypeId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.studyId", is(dataset.getStudyId())))

			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].termId", is(dataset.getVariables().get(0).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].name", is(dataset.getVariables().get(0).getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].description", is(dataset.getVariables().get(0).getDescription())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].scale", is(dataset.getVariables().get(0).getScale())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].method", is(dataset.getVariables().get(0).getMethod())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].property", is(dataset.getVariables().get(0).getProperty())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].dataType", is(dataset.getVariables().get(0).getDataType())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].dataTypeId", is(dataset.getVariables().get(0).getDataTypeId())))

			.andExpect(MockMvcResultMatchers.jsonPath("$.instances[0].instanceDbId", is(dataset.getInstances().get(0).getInstanceDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.instances[0].locationName", is(dataset.getInstances().get(0).getLocationName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.instances[0].locationAbbreviation", is(dataset.getInstances().get(0).getLocationAbbreviation())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.instances[0].customLocationAbbreviation", is(dataset.getInstances().get(0).getCustomLocationAbbreviation())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.instances[0].instanceNumber", is(dataset.getInstances().get(0).getInstanceNumber())));

		verify(this.studyDatasetService, times(1)).getDataset(this.cropName, 100,101);
	}

	@Test
	public void testGetDatasetErrorStudyIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		errors.reject("study.not.exist", "");
		Mockito.when(studyDatasetService.getDataset(this.cropName, 1000,1001)).thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets/{datasetId}", this.cropName, 1000,1001)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("Study does not exist")));
		verify(this.studyDatasetService, times(1)).getDataset(this.cropName, 1000,1001);
		verifyNoMoreInteractions(this.studyDatasetService);
	}

	@Test
	public void testGetDatasetErrorDatasetIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Set<Integer> datasetTypeIds = new TreeSet<>();
		datasetTypeIds.add(100);
		errors.reject("dataset.does.not.exist", "");
		Mockito.when(studyDatasetService.getDataset(this.cropName, 1011,1012)).thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/studies/{studyId}/datasets/{datasetId}", this.cropName, 1011,1012)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("Dataset does not exist")));
		verify(this.studyDatasetService, times(1)).getDataset(this.cropName, 1011,1012);
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

	private DatasetDTO createDataset(final int datasetType, final Integer datasetId,final String name, final String crop, final Integer studyId) {
		final DataSetType dataSetType = DataSetType.findById(datasetType);

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(datasetId);
		datasetDTO.setName(name);
		datasetDTO.setCropName(crop);
		datasetDTO.setDatasetTypeId(dataSetType.getId());
		datasetDTO.setStudyId(studyId);

		final List<MeasurementVariable> variables = new ArrayList<>();
		final List<StudyInstance> instances = new ArrayList<>();

		final MeasurementVariable variable = new MeasurementVariable();

		variable.setTermId(8206);
		variable.setName("PLANT_NO");
		variable.setDescription("Enumerator for the observed plant");
		variable.setScale("Number");
		variable.setMethod("Enumerated");
		variable.setProperty("Plants Observed");
		variable.setDataType("Numeric");
		variable.setDataTypeId(1110);

		variables.add(variable);
		datasetDTO.setVariables(variables);

		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceDbId(67);
		studyInstance.setLocationName("CENTER FOR INTERNATIONAL FORESTRY RESEARCH");
		studyInstance.setLocationAbbreviation("CIFOR");
		studyInstance.setInstanceNumber(1);

		instances.add(studyInstance);
		datasetDTO.setInstances(instances);

		return datasetDTO;
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

}
