package org.ibp.api.rest.dataset;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
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
import java.util.Set;
import java.util.TreeSet;

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
	public void testCountPhenotypesForValidDataset() throws Exception {
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
		Mockito.when(studyDatasetService.getDatasets(100, null)).thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

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
		errors.reject("dataset.type.id.not.exist", new Object[]{100},"");
		Mockito.when(studyDatasetService.getDatasets(100,filterByTypeIds)).thenThrow(new ResourceNotFoundException(errors.getAllErrors().get(0)));

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
}
