package org.ibp.api.rest.dataset;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.data.initializer.DatasetTypeTestDataInitializer;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.dataset.FilteredPhenotypesInstancesCountDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsParamDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.PreconditionFailedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.DatasetCollectionOrderServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DatasetResourceTest extends ApiUnitTestBase {

	private static final String DATASET_NAME = "TEST1234";
	private static final int STUDY_ID = 12345;
	private static final int PARENT_ID = 200;
	private static final String DATASETS_GENERATION_URL =
		"/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/{parentId}/generation";
	private final MediaType xlsContentType =
		new MediaType(MediaType.APPLICATION_OCTET_STREAM.getType(), MediaType.APPLICATION_OCTET_STREAM.getSubtype(),
			Charset.forName("utf8"));

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private DatasetExportService datasetCSVExportService;

	@Mock
	private HttpServletRequest request;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public DatasetService studyDatasetService() {
			return Mockito.mock(DatasetService.class);
		}

		@Bean
		@Primary
		public DatasetExportService datasetCSVExportService() {
			return Mockito.mock(DatasetExportService.class);
		}
	}

	@Before
	public void setup() throws Exception {
		super.setUp();
	}

	@Test
	public void testCountPhenotypes() throws Exception {
		final long count = 10;
		doReturn(count).when(this.studyDatasetService).countObservationsByVariables(100, 102, Arrays.asList(1, 2, 3));

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.head("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/variables/observations", this.cropName,
					this.programUuid, 100, 102)
				.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}

	@Test
	public void testCountPhenotypesByObservation() throws Exception {
		final long count = 11;
		doReturn(count).when(this.studyDatasetService).countObservationsByInstance(100, 102, 103);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.head("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{instanceId}",
					this.cropName, this.programUuid, 100, 102, 103)
				.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}

	@Test
	public void testAddDatasetSelectionVariable() throws Exception {
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int traitId = random.nextInt(10000);
		final String alias = RandomStringUtils.randomAlphabetic(20);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), traitId, alias);
		doReturn(measurementVariable).when(this.studyDatasetService)
			.addDatasetVariable(studyId, datasetId, datasetVariable, VariableType.SELECTION_METHOD);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.put("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/selections", this.cropName,
					this.programUuid, studyId, datasetId)
				.contentType(this.contentType).content(this.convertObjectToByte(datasetVariable)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testRemoveTraitVariables() throws Exception {
		final int studyId = 100;
		final int datasetId = 102;
		this.mockMvc
			.perform(MockMvcRequestBuilders
				.delete("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/traits", this.cropName,
					this.programUuid, studyId, datasetId)
				.param("variableIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
		Mockito.verify(this.studyDatasetService).removeDatasetVariables(studyId, datasetId, Arrays.asList(1, 2, 3), VariableType.TRAIT);
	}

	@Test
	public void testGetVariables() throws Exception {
		final int studyId = 100;
		final int datasetId = 102;
		final int variableTypeId = VariableType.SELECTION_METHOD.getId();
		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/variables/{variableTypeId}", this.cropName,
					this.programUuid, studyId, datasetId,
					variableTypeId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
		Mockito.verify(this.studyDatasetService).getDatasetVariablesByType(studyId, datasetId, VariableType.getById(variableTypeId));
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
				.post(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations",
					this.cropName, this.programUuid, studyId,
					datasetId, observationUnitId)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(observationDto)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.studyDatasetService)
			.createObservation(ArgumentMatchers.eq(studyId), ArgumentMatchers.eq(datasetId), ArgumentMatchers.eq(observationUnitId),
				ArgumentMatchers.any(ObservationDto.class));
	}

	@Test
	public void testUpdateObservation() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int observationUnitId = random.nextInt(10000);
		final int observationId = random.nextInt(10000);
		final ObservationDto observationDto = new ObservationDto();

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.patch(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}",
					this.cropName, this.programUuid, studyId, datasetId, observationUnitId, observationId)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(observationDto)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.studyDatasetService)
			.updateObservation(ArgumentMatchers.eq(studyId), ArgumentMatchers.eq(datasetId), ArgumentMatchers.eq(observationId),
				ArgumentMatchers.eq(observationUnitId),
				ArgumentMatchers.any(ObservationDto.class));
	}

	@Test
	public void testGetDatasets() throws Exception {
		final List<DatasetDTO> datasets = this.createDatasets(
			Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId(), DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId(),
				DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId()));
		doReturn(datasets).when(this.studyDatasetService).getDatasets(100, null);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets", this.cropName,
					this.programUuid, 100)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(datasets.size())));
	}

	@Test
	public void testGetDatasetsWithFilter() throws Exception {
		final List<DatasetDTO> datasets = this.createDatasets(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()));
		doReturn(datasets).when(this.studyDatasetService).getDatasets(100, null);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets", this.cropName,
					this.programUuid, 100)
				.param("datasetTypeIds", "4").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(datasets.size())));
	}

	@Test
	public void testGetDatasetsErrorStudyIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		errors.reject("study.not.exist", "");
		Mockito.when(this.studyDatasetService.getDatasets(100, null))
			.thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(
				MockMvcRequestBuilders.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets", this.cropName, this.programUuid,
						100)
					.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is("Study does not exist")));

	}

	@Test
	public void testGetDatasetsErrorDatasetTypeIdIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Set<Integer> datasetTypeIds = new TreeSet<>();
		datasetTypeIds.add(100);
		errors.reject("dataset.type.id.not.exist", new Object[] {100}, "");
		Mockito.when(this.studyDatasetService.getDatasets(100, datasetTypeIds))
			.thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(
				MockMvcRequestBuilders.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets", this.cropName, this.programUuid,
						100)
					.param("datasetTypeIds", "100").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is("100 is not a valid dataset type")));
	}

	@Test
	public void testGetDataset() throws Exception {
		final DatasetDTO dataset = this.createDataset(DatasetTypeEnum.PLOT_DATA.getId(), 101101, "Plant", this.cropName, 100);
		doReturn(dataset).when(this.studyDatasetService).getDataset(this.cropName, 100, 101);

		this.mockMvc
			.perform(
				MockMvcRequestBuilders.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}", this.cropName,
						this.programUuid, 100, 101)
					.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.datasetId", is(dataset.getDatasetId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.name", is(dataset.getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.cropName", is(dataset.getCropName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.datasetTypeId", is(dataset.getDatasetTypeId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.studyId", is(dataset.getStudyId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.hasPendingData", is(dataset.getHasPendingData())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].termId", is(dataset.getVariables().get(0).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].name", is(dataset.getVariables().get(0).getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].description", is(dataset.getVariables().get(0).getDescription())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].scale", is(dataset.getVariables().get(0).getScale())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].method", is(dataset.getVariables().get(0).getMethod())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].property", is(dataset.getVariables().get(0).getProperty())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].dataType", is(dataset.getVariables().get(0).getDataType())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].dataTypeId", is(dataset.getVariables().get(0).getDataTypeId())))

			.andExpect(MockMvcResultMatchers.jsonPath("$.instances[0].locationName", is(dataset.getInstances().get(0).getLocationName())))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.instances[0].locationAbbreviation", is(dataset.getInstances().get(0).getLocationAbbreviation())))
			.andExpect(MockMvcResultMatchers.jsonPath(
				"$.instances[0].customLocationAbbreviation",
				is(dataset.getInstances().get(0).getCustomLocationAbbreviation())))
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.instances[0].instanceNumber", is(dataset.getInstances().get(0).getInstanceNumber())));

		verify(this.studyDatasetService, times(1)).getDataset(this.cropName, 100, 101);
	}

	@Test
	public void testGetDatasetErrorStudyIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		errors.reject("study.not.exist", "");
		Mockito.when(this.studyDatasetService.getDataset(this.cropName, 1000, 1001)).thenThrow(
			new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(
				MockMvcRequestBuilders.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}", this.cropName,
						this.programUuid, 1000, 1001)
					.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is("Study does not exist")));
		verify(this.studyDatasetService, times(1)).getDataset(this.cropName, 1000, 1001);
	}

	@Test
	public void testGetDatasetErrorDatasetIsNotFound() throws Exception {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Set<Integer> datasetTypeIds = new TreeSet<>();
		datasetTypeIds.add(100);
		errors.reject("dataset.does.not.exist", "");
		Mockito.when(this.studyDatasetService.getDataset(this.cropName, 1011, 1012)).thenThrow(
			new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.mockMvc.perform(
				MockMvcRequestBuilders.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}", this.cropName,
						this.programUuid, 1011, 1012)
					.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is("Dataset does not exist")));
		verify(this.studyDatasetService, times(1)).getDataset(this.cropName, 1011, 1012);
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

		Mockito.when(this.studyDatasetService.countAllObservationUnitsForDataset(
				ArgumentMatchers.anyInt(),
				ArgumentMatchers.anyList(), ArgumentMatchers.anyBoolean()))
			.thenReturn(100);
		Mockito.when(this.studyDatasetService.countFilteredObservationUnitsForDataset(
				ArgumentMatchers.anyInt(),
				ArgumentMatchers.anyList(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any()))
			.thenReturn(100L);
		Mockito.when(this.studyDatasetService.getObservationUnitRows(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(),
				ArgumentMatchers.any(), ArgumentMatchers.any()))
			.thenReturn(Lists.newArrayList(obsDto));
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int instanceId = random.nextInt(10000);

		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
		searchDTO.setInstanceIds(Arrays.asList(instanceId));

		this.mockMvc
			.perform(MockMvcRequestBuilders.post(
				"/crops/{cropname}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/table",
				this.cropName,
				this.programUuid,
				studyId,
				datasetId).content(this.convertObjectToByte(searchDTO)).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", "100"))
			.andExpect(MockMvcResultMatchers.header().string("X-Filtered-Count", "100"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].observationUnitId", is(obsDto.getObservationUnitId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].gid", is(obsDto.getGid())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].designation", is(obsDto.getDesignation())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].action", is(obsDto.getAction())))
			.andExpect(
				MockMvcResultMatchers.jsonPath(
					"$[0].variables[\"TEST1\"].observationId",
					is(measurement.getObservationId())))
			.andExpect(
				MockMvcResultMatchers.jsonPath(
					"$[0].variables[\"TEST1\"].categoricalValueId",
					is(measurement.getCategoricalValueId())))
			.andExpect(
				MockMvcResultMatchers.jsonPath(
					"$[0].variables[\"TEST1\"].value",
					is(measurement.getValue())))
			.andExpect(MockMvcResultMatchers.jsonPath(
				"$[0].variables[\"TEST1\"].status",
				is(measurement.getStatus().getName())))
		;
	}

	@Test
	public void testGetObservationUnitRowsAsListMap() throws Exception {

		final Map<String, Object> rowDataMap = new HashMap<>();
		rowDataMap.put("TRIAL_INSTANCE", "1");
		rowDataMap.put("TRAIT1", 1);
		rowDataMap.put("TRAIT2", "ABC");

		Mockito.when(this.studyDatasetService.getObservationUnitRowsAsMapList(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(),
				ArgumentMatchers.any()))
			.thenReturn(Lists.newArrayList(rowDataMap));
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int instanceId = random.nextInt(10000);

		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
		searchDTO.setInstanceIds(Arrays.asList(instanceId));
		searchDTO.getVisibleColumns().add("TRIAL_INSTANCE");
		searchDTO.getVisibleColumns().add("TRAIT1");
		searchDTO.getVisibleColumns().add("TRAIT2");

		this.mockMvc
			.perform(MockMvcRequestBuilders.post(
				"/crops/{cropname}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/mapList",
				this.cropName,
				this.programUuid,
				studyId,
				datasetId).content(this.convertObjectToByte(searchDTO)).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].TRIAL_INSTANCE", is("1")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].TRAIT1", is(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].TRAIT2", is("ABC")));
	}

	@Test
	public void testDeleteObservation() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int observationUnitId = random.nextInt(10000);
		final int observationId = random.nextInt(10000);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.delete(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}",
					this.cropName, this.programUuid, studyId, datasetId, observationUnitId, observationId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.studyDatasetService)
			.deleteObservation(ArgumentMatchers.eq(studyId), ArgumentMatchers.eq(datasetId), ArgumentMatchers.eq(observationUnitId),
				ArgumentMatchers.eq(observationId));
	}

	@Test
	public void testGenerateDataset() throws Exception {
		final int datasetTypeId = DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId();
		final DatasetDTO dataset = this.createDataset(datasetTypeId, 1234, DATASET_NAME, this.cropName, STUDY_ID);
		final DatasetGeneratorInput datasetGeneratorInput = new DatasetGeneratorInput();
		final Integer[] instanceIds = new Integer[1];

		datasetGeneratorInput.setDatasetName(DATASET_NAME);
		datasetGeneratorInput.setDatasetTypeId(datasetTypeId);
		datasetGeneratorInput.setInstanceIds(instanceIds);
		datasetGeneratorInput.setNumberOfSubObservationUnits(5);
		datasetGeneratorInput.setSequenceVariableId(8206);

		doReturn(dataset).when(this.studyDatasetService)
			.generateSubObservationDataset(this.cropName, STUDY_ID, PARENT_ID, datasetGeneratorInput);
		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					DATASETS_GENERATION_URL, this.cropName, this.programUuid, STUDY_ID, PARENT_ID,
					datasetGeneratorInput)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(datasetGeneratorInput)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.name", is(dataset.getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.datasetId", is(dataset.getDatasetId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.datasetTypeId", is(dataset.getDatasetTypeId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.studyId", is(dataset.getStudyId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.cropName", is(dataset.getCropName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].termId", is(dataset.getVariables().get(0).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].name", is(dataset.getVariables().get(0).getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].description", is(dataset.getVariables().get(0).getDescription())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].scale", is(dataset.getVariables().get(0).getScale())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].method", is(dataset.getVariables().get(0).getMethod())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].property", is(dataset.getVariables().get(0).getProperty())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].dataType", is(dataset.getVariables().get(0).getDataType())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.variables[0].dataTypeId", is(dataset.getVariables().get(0).getDataTypeId())))

			.andExpect(MockMvcResultMatchers.jsonPath("$.instances[0].locationName", is(dataset.getInstances().get(0).getLocationName())))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.instances[0].locationAbbreviation", is(dataset.getInstances().get(0).getLocationAbbreviation())))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.instances[0].customLocationAbbreviation", is(dataset.getInstances().get(0).getCustomLocationAbbreviation())))
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.instances[0].instanceNumber", is(dataset.getInstances().get(0).getInstanceNumber())));
	}

	@Test
	public void testGenerateDatasetsErrorDuplicatedName() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = new DatasetGeneratorInput();
		final Integer[] instanceIds = new Integer[1];
		final int datasetTypeId = DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId();
		datasetGeneratorInput.setDatasetName(DATASET_NAME);
		datasetGeneratorInput.setDatasetTypeId(datasetTypeId);
		datasetGeneratorInput.setInstanceIds(instanceIds);
		datasetGeneratorInput.setNumberOfSubObservationUnits(5);
		datasetGeneratorInput.setSequenceVariableId(8206);
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		errors.reject("dataset.name.not.available", "");
		Mockito.when(this.studyDatasetService.generateSubObservationDataset(this.cropName, STUDY_ID, PARENT_ID, datasetGeneratorInput))
			.thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

		this.generateAndValidateDataset(datasetGeneratorInput, "{0} is already in use.");
	}

	@Test
	public void testGenerateDatasetsErrorDatasetTypeNotExists() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = this.setupDatasetGeneratorInput("dataset.type.id.not.exist");

		this.generateAndValidateDataset(datasetGeneratorInput, "{0} is not a valid dataset type");
	}

	@Test
	public void testGenerateDatasetsErrorParentNotAllowed() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = this.setupDatasetGeneratorInput("dataset.parent.not.allowed");

		this.generateAndValidateDataset(datasetGeneratorInput, "Specified parentId does not have type PLOT or SubObservation type");
	}

	@Test
	public void testGenerateDatasetsErrorMaxChildren() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = this.setupDatasetGeneratorInput("dataset.creation.not.allowed");

		this.generateAndValidateDataset(datasetGeneratorInput, "The parent dataset already has {0} children datasets");
	}

	@Test
	public void testGenerateDatasetsErrorExceedLength() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = this.setupDatasetGeneratorInput("dataset.name.exceed.length");

		this.generateAndValidateDataset(datasetGeneratorInput, "Dataset name must not exceed 100 characters.");
	}

	@Test
	public void testGenerateDatasetsErrorEmptyName() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = this.setupDatasetGeneratorInput("dataset.name.empty.name");

		this.generateAndValidateDataset(datasetGeneratorInput, "Dataset name should not be empty.");
	}

	@Test
	public void testGenerateDatasetsErrorInvalidInstances() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = this.setupDatasetGeneratorInput("dataset.invalid.instances");

		this.generateAndValidateDataset(
			datasetGeneratorInput, "Specified instance(s) does not belong to the study or array is empty");
	}

	@Test
	public void testGenerateDatasetsErrorInvalidSubObservationUnitVariable() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput =
			this.setupDatasetGeneratorInput("dataset.invalid.obs.unit.variable");

		this.generateAndValidateDataset(datasetGeneratorInput, "Invalid SubObservation Unit VariableId: {0}.");
	}

	@Test
	public void testGenerateDatasetsErrorInvalidSubObservationUnitNumber() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput =
			this.setupDatasetGeneratorInput("dataset.invalid.number.subobs.units");

		this.generateAndValidateDataset(
			datasetGeneratorInput, "Number of subobservations units should not be lower than 1 or higher than {0}.");
	}

	@Test
	public void testGenerateDatasetsErrorNotImplementedForDataset() throws Exception {
		final DatasetGeneratorInput datasetGeneratorInput = this.setupDatasetGeneratorInput("dataset.operation.not.implemented");

		this.generateAndValidateDataset(datasetGeneratorInput, "This operation is not implemented for the dataset type id: {0}");
	}

	private void generateAndValidateDataset(final DatasetGeneratorInput datasetGeneratorInput, final String message) throws Exception {
		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					DATASETS_GENERATION_URL, this.cropName, this.programUuid, STUDY_ID, PARENT_ID,
					datasetGeneratorInput)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(datasetGeneratorInput)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.errors[0].message", is(message)));
	}

	private DatasetGeneratorInput setupDatasetGeneratorInput(final String message) {
		Mockito.reset(this.studyDatasetService);
		final DatasetGeneratorInput datasetGeneratorInput = new DatasetGeneratorInput();
		final Integer[] instanceIds = new Integer[1];
		final int datasetTypeId = DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId();
		datasetGeneratorInput.setDatasetName(DATASET_NAME);
		datasetGeneratorInput.setDatasetTypeId(datasetTypeId);
		datasetGeneratorInput.setInstanceIds(instanceIds);
		datasetGeneratorInput.setNumberOfSubObservationUnits(5);
		datasetGeneratorInput.setSequenceVariableId(8206);
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		errors.reject(message, "");
		Mockito.when(this.studyDatasetService.generateSubObservationDataset(this.cropName, STUDY_ID, PARENT_ID, datasetGeneratorInput))
			.thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));
		return datasetGeneratorInput;
	}

	@Test
	public void testGetObservationUnitAsCSV() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final Set<Integer> instanceIds = new HashSet<>();
		instanceIds.add(1);
		instanceIds.add(2);
		instanceIds.add(3);
		final int collectionOrderId = DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId();

		final File file = File.createTempFile("test", ".csv");
		Mockito.when(this.datasetCSVExportService.export(studyId, datasetId, instanceIds, collectionOrderId, false, false))
			.thenReturn(file);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/{fileType}",
					this.cropName, this.programUuid, studyId, datasetId, DatasetResource.CSV)
				.param("instanceIds", "1,2,3")
				.param("collectionOrderId", String.valueOf(collectionOrderId))
				.param("singleFile", String.valueOf(false))
				.param("includeSampleGenotypeValues", String.valueOf(false))
				.contentType(this.csvContentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	public void testGetObservationUnitAsExcel() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final Set<Integer> instanceIds = new HashSet<>();
		instanceIds.add(1);
		instanceIds.add(2);
		instanceIds.add(3);
		final int collectionOrderId = DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId();

		final File file = File.createTempFile("test", ".xls");
		Mockito.when(this.datasetCSVExportService.export(studyId, datasetId, instanceIds, collectionOrderId, false, false))
			.thenReturn(file);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/{fileType}",
					this.cropName, this.programUuid, studyId, datasetId, DatasetResource.CSV)
				.param("instanceIds", "1,2,3")
				.param("collectionOrderId", String.valueOf(collectionOrderId))
				.param("singleFile", String.valueOf(false))
				.param("includeSampleGenotypeValues", String.valueOf(false))
				.contentType(this.xlsContentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	public void testImportDataset_WarningExpected() throws Exception {
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		observationsPutRequestInput.setProcessWarnings(true);
		observationsPutRequestInput.setData(new ArrayList<>());
		final PreconditionFailedException exception = new PreconditionFailedException(new ArrayList<>());
		Mockito.doThrow(exception).when(this.studyDatasetService).importObservations(studyId, datasetId, observationsPutRequestInput);
		this.mockMvc.perform(MockMvcRequestBuilders
				.put("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/observations", this.cropName,
					this.programUuid, studyId,
					datasetId).contentType(this.contentType).content(this.convertObjectToByte(observationsPutRequestInput)))
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	@Test
	public void testImportDataset_Ok() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		observationsPutRequestInput.setProcessWarnings(false);
		observationsPutRequestInput.setData(new ArrayList<>());
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) {
				return null;
			}
		}).when(this.studyDatasetService).importObservations(studyId, datasetId, observationsPutRequestInput);
		this.mockMvc.perform(MockMvcRequestBuilders
				.put("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/observations", this.cropName,
					this.programUuid, studyId,
					datasetId).contentType(this.contentType).content(this.convertObjectToByte(observationsPutRequestInput)))
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testImportDataset_ErrorExpected() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		observationsPutRequestInput.setProcessWarnings(false);
		observationsPutRequestInput.setData(new ArrayList<>());
		final ApiRequestValidationException exception = new ApiRequestValidationException(new ArrayList<>());
		Mockito.doThrow(exception).when(this.studyDatasetService).importObservations(studyId, datasetId, observationsPutRequestInput);
		this.mockMvc.perform(MockMvcRequestBuilders
				.put("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observationUnits/observations", this.cropName,
					this.programUuid, studyId,
					datasetId).contentType(this.contentType).content(this.convertObjectToByte(observationsPutRequestInput)))
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void testAcceptDrafData() throws Exception {
		final DatasetDTO dataset = this.createDataset(DatasetTypeEnum.PLOT_DATA.getId(), 101101, "Plant", this.cropName, 100);

		final int studyId = dataset.getStudyId();
		final int datasetId = dataset.getDatasetId();

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/acceptance",
					this.cropName, this.programUuid, studyId,
					datasetId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testDiscardDrafData() throws Exception {
		final DatasetDTO dataset = this.createDataset(DatasetTypeEnum.PLOT_DATA.getId(), 101101, "Plant", this.cropName, 100);

		final int studyId = dataset.getStudyId();
		final int datasetId = dataset.getDatasetId();

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/rejection",
					this.cropName, this.programUuid, studyId,
					datasetId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testCheckWithoutOutOfBoundDraftData() throws Exception {
		final DatasetDTO dataset = this.createDataset(DatasetTypeEnum.PLOT_DATA.getId(), 101101, "Plant", this.cropName, 100);

		final int studyId = dataset.getStudyId();
		final int datasetId = dataset.getDatasetId();

		Mockito.when(this.studyDatasetService.hasDatasetDraftDataOutOfBounds(studyId, datasetId)).thenReturn(false);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/out-of-bounds",
					this.cropName, this.programUuid, studyId,
					datasetId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void testCheckWithOutOfBoundDraftData() throws Exception {
		final DatasetDTO dataset = this.createDataset(DatasetTypeEnum.PLOT_DATA.getId(), 101101, "Plant", this.cropName, 100);

		final int studyId = dataset.getStudyId();
		final int datasetId = dataset.getDatasetId();

		Mockito.when(this.studyDatasetService.hasDatasetDraftDataOutOfBounds(studyId, datasetId)).thenReturn(true);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/out-of-bounds",
					this.cropName, this.programUuid, studyId, datasetId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testSetAsMissingDrafData() throws Exception {
		final DatasetDTO dataset = this.createDataset(DatasetTypeEnum.PLOT_DATA.getId(), 101101, "Plant", this.cropName, 100);

		final int studyId = dataset.getStudyId();
		final int datasetId = dataset.getDatasetId();

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/set-as-missing",
					this.cropName,
					this.programUuid,
					studyId,
					datasetId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testSetValueToVariable() throws Exception {
		final ObservationUnitsParamDTO paramDTO = new ObservationUnitsParamDTO();
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int instanceId = random.nextInt(10000);

		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
		searchDTO.setInstanceIds(Arrays.asList(instanceId));

		paramDTO.setObservationUnitsSearchDTO(searchDTO);
		paramDTO.setNewValue("123");
		paramDTO.setNewCategoricalValueId(12345);
		searchDTO.setDatasetId(datasetId);
		paramDTO.getObservationUnitsSearchDTO().getFilter().setVariableId(555);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.ADMIN.name())).thenReturn(true);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post("/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/filter/set-value",
					this.cropName, this.programUuid, studyId,
					datasetId)
				.contentType(this.contentType).content(this.convertObjectToByte(paramDTO)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testAcceptDraftDataByVariable() throws Exception {
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);
		final int instanceId = random.nextInt(10000);

		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
		searchDTO.setInstanceIds(Arrays.asList(instanceId));
		searchDTO.setDatasetId(datasetId);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.ADMIN.name())).thenReturn(true);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/drafts/filter/acceptance",
					this.cropName,
					this.programUuid,
					studyId,
					datasetId)
				.contentType(this.contentType).content(this.convertObjectToByte(searchDTO)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testCountFilteredInstancesAndObservationUnits_Ok() throws Exception {
		final Random random = new Random();
		final int studyId = random.nextInt(10000);
		final int datasetId = random.nextInt(10000);

		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
		searchDTO.setDatasetId(datasetId);

		final Integer totalInstances = 1;
		final Integer totalPhenotypes = 5;
		final FilteredPhenotypesInstancesCountDTO result = new FilteredPhenotypesInstancesCountDTO(totalPhenotypes, totalInstances);

		Mockito.when(this.studyDatasetService
				.countFilteredInstancesAndObservationUnits(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(ObservationUnitsSearchDTO.class)))
			.thenReturn(result);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observation-units/observations/filter/count",
					this.cropName,
					this.programUuid,
					studyId,
					datasetId)
				.contentType(this.contentType).content(this.convertObjectToByte(searchDTO)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.totalFilteredPhenotypes", is(totalPhenotypes)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.totalFilteredInstances", is(totalInstances)));
	}

	private DatasetDTO createDataset(
		final int datasetTypeId, final Integer datasetId, final String name, final String crop, final Integer studyId) {

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(datasetId);
		datasetDTO.setName(name);
		datasetDTO.setCropName(crop);
		datasetDTO.setDatasetTypeId(datasetTypeId);
		datasetDTO.setStudyId(studyId);
		datasetDTO.setHasPendingData(Boolean.FALSE);

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
		studyInstance.setInstanceId(67);
		studyInstance.setLocationName("CENTER FOR INTERNATIONAL FORESTRY RESEARCH");
		studyInstance.setLocationAbbreviation("CIFOR");
		studyInstance.setInstanceNumber(1);

		instances.add(studyInstance);
		datasetDTO.setInstances(instances);

		return datasetDTO;
	}

	private List<DatasetDTO> createDatasets(final List<Integer> datasetTypeIds) {
		final List<DatasetDTO> datasets = new ArrayList<>();
		int num = datasetTypeIds.size();
		final Map<Integer, DatasetTypeDTO> datasetTypeMap = DatasetTypeTestDataInitializer.createDatasetTypes();
		for (final Integer datasetTypeId : datasetTypeIds) {
			final DatasetDTO datasetDTO = new DatasetDTO();
			datasetDTO.setDatasetTypeId(datasetTypeId);
			datasetDTO.setName(datasetTypeMap.get(datasetTypeId).getName() + "_" + num);
			datasetDTO.setHasPendingData(Boolean.TRUE);
			num--;
		}
		return datasets;
	}

}
