package org.ibp.api.rest.derived;

import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.derived.DerivedVariableService;
import org.ibp.api.java.impl.middleware.derived.DerivedVariableServiceImpl;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.ObjectError;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DerivedVariableResourceTest extends ApiUnitTestBase {

	@Resource
	private DerivedVariableService derivedVariableService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public DerivedVariableService derivedVariableService() {
			return mock(DerivedVariableService.class);
		}
	}

	@Test
	public void testCalculateSuccess() throws Exception {

		final CalculateVariableRequest calculateVariableRequest = new CalculateVariableRequest();
		calculateVariableRequest.setVariableId(RandomUtils.nextInt());
		calculateVariableRequest.setOverwriteExistingData(false);
		calculateVariableRequest.setGeoLocationIds(Arrays.asList(RandomUtils.nextInt()));

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/derived-variables/calculation", this.cropName, this.programUuid, 100,
					102)
				.contentType(this.contentType).content(this.convertObjectToByte(calculateVariableRequest)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		verify(this.derivedVariableService)
			.execute(100, 102, calculateVariableRequest.getVariableId(), calculateVariableRequest.getGeoLocationIds(),
				calculateVariableRequest.getInputVariableDatasetMap(),
				calculateVariableRequest.isOverwriteExistingData());

	}

	@Test
	public void testCalculateOverwriteWarning() throws Exception {

		final CalculateVariableRequest calculateVariableRequest = new CalculateVariableRequest();
		calculateVariableRequest.setVariableId(RandomUtils.nextInt());
		calculateVariableRequest.setOverwriteExistingData(false);
		calculateVariableRequest.setGeoLocationIds(Arrays.asList(RandomUtils.nextInt()));

		final OverwriteDataException exception = new OverwriteDataException(null);

		when(this.derivedVariableService
			.execute(100, 102, calculateVariableRequest.getVariableId(), calculateVariableRequest.getGeoLocationIds(),
				calculateVariableRequest.getInputVariableDatasetMap(),
				calculateVariableRequest.isOverwriteExistingData())).thenThrow(exception);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					"/crops/{crop}/programs/{programUUID}, /studies/{studyId}/datasets/{datasetId}/derived-variables/calculation", this.cropName, this.programUuid, 100,
					102)
				.contentType(this.contentType).content(this.convertObjectToByte(calculateVariableRequest)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("{\"hasDataOverwrite\":true}"));

	}

	@Test
	public void testGetMissingFormulaVariablesInStudy() throws Exception {

		final Set<FormulaVariable> formulaVariables = new HashSet<>();
		formulaVariables.add(new FormulaVariable(1, "VAR1", 3));
		formulaVariables.add(new FormulaVariable(2, "VAR2", 3));

		doReturn(formulaVariables).when(this.derivedVariableService)
			.getMissingFormulaVariablesInStudy(100, 101, 103);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/derived-variables/{variableId}/formula-variables/missing",
					this.cropName, this.programUuid, 100, 101, 103)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string(
				"[{\"id\":1,\"vocabularyId\":0,\"name\":\"VAR1\",\"definition\":null,\"obsolete\":false,\"dateCreated\":null,"
					+ "\"dateLastModified\":null,\"targetTermId\":3},{\"id\":2,\"vocabularyId\":0,\"name\":\"VAR2\",\"definition\":null,"
					+ "\"obsolete\":false,\"dateCreated\":null,\"dateLastModified\":null,\"targetTermId\":3}]"));

	}

	@Test
	public void testGetAllFormulaVariablesInStudy() throws Exception {

		final Set<FormulaVariable> formulaVariables = new HashSet<>();
		formulaVariables.add(new FormulaVariable(1, "VAR1", 3));
		formulaVariables.add(new FormulaVariable(2, "VAR2", 3));

		doReturn(formulaVariables).when(this.derivedVariableService)
			.getFormulaVariablesInStudy(100, 101);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/derived-variables/formula-variables/",
					this.cropName, this.programUuid, 100, 101)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string(
				"[{\"id\":1,\"vocabularyId\":0,\"name\":\"VAR1\",\"definition\":null,\"obsolete\":false,\"dateCreated\":null,"
					+ "\"dateLastModified\":null,\"targetTermId\":3},{\"id\":2,\"vocabularyId\":0,\"name\":\"VAR2\",\"definition\":null,"
					+ "\"obsolete\":false,\"dateCreated\":null,\"dateLastModified\":null,\"targetTermId\":3}]"));

	}

	@Test
	public void testCountCalculatedVariables() throws Exception {
		final long count = 10;

		doReturn(count).when(this.derivedVariableService)
			.countCalculatedVariablesInDatasets(100, new HashSet<Integer>(Arrays.asList(1, 2, 3)));

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.head("/crops/{crop}/programs/{programUUID}/studies/{studyId}/derived-variables", this.cropName, this.programUuid, 100, 102)
				.param("datasetIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}

	@Test
	public void testGetFormulaVariableDatasetMap() throws Exception {

		final int termId = 1;
		final String variableName = "some name";
		final Map<Integer, Map<String, Object>> formulaVariablesDatasetMap = new HashMap<>();
		final Map<String, Object> values = new HashMap<>();
		values.put("variableName", variableName);
		values.put("datasets", Arrays.asList(new DatasetDTO()));
		formulaVariablesDatasetMap.put(termId, values);

		doReturn(formulaVariablesDatasetMap).when(this.derivedVariableService)
			.getFormulaVariableDatasetsMap(100, 101, 103);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/derived-variables/{variableId}/formula-variables/dataset-map",
					this.cropName, this.programUuid, 100, 101, 103)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string(
				"{\"1\":{\"variableName\":\"some name\",\"datasets\":[{}]}}"));

	}

}
