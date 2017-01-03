package org.ibp.api.brapi.v1.study;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.generationcp.middleware.service.api.study.StudyService;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class StudyResourceBrapiTest extends ApiUnitTestBase {

	@SuppressWarnings("unused")
	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService studyServiceMW;

	@Test
	public void testListStudySummaries() throws Exception {

		// TODO with StudyResourceBrapi implementation
	}


	@Test
	public void testGetStudyObservationsAsTable() throws Exception {

		final int trialDbId = current().nextInt();
		final int studyDbId = current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();

		final List<List<String>> data = ImmutableList.<List<String>>builder().add(ImmutableList.<String>builder().add(randomAlphabetic(5))
				.add(randomAlphabetic(5)).add(randomAlphabetic(5)).add(randomAlphabetic(5)).build()).build();

		TrialObservationTable trialObservationTable = new TrialObservationTable().setStudyDbId(trialDbId).setObservationVariableDbIds(observationVariablesId)
				.setObservationVariableNames(observationVariableName).setData(data);

		Mockito.when(this.studyServiceMW.getTrialObservationTable(trialDbId, studyDbId)).thenReturn(trialObservationTable);

		UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies/{studyDbId}/table")
				.queryParam("trialDbId", trialDbId)
				.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build()).encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print()).andExpect(jsonPath("$.result.studyDbId", is(trialObservationTable.getStudyDbId())))
				.andExpect(jsonPath("$.result.observationVariableDbIds", IsCollectionWithSize.hasSize(observationVariablesId.size())))
				.andExpect(jsonPath("$.result.observationVariableDbIds[0]", is(observationVariablesId.get(0))))
				.andExpect(jsonPath("$.result.observationVariableNames", IsCollectionWithSize.hasSize(observationVariableName.size())))
				.andExpect(jsonPath("$.result.observationVariableNames[0]", is(observationVariableName.get(0))))
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(data.size())))
				.andExpect(jsonPath("$.result.data[0][0]", is(data.get(0).get(0))))
				.andExpect(jsonPath("$.result.data[0][1]", is(data.get(0).get(1))))
				.andExpect(jsonPath("$.result.data[0][2]", is(data.get(0).get(2))))
				.andExpect(jsonPath("$.result.data[0][3]", is(data.get(0).get(3))))
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", is(1)))
				.andExpect(jsonPath("$.metadata.pagination.pageSize", is(1)))
				.andExpect(jsonPath("$.metadata.pagination.totalCount", is(1)))
				.andExpect(jsonPath("$.metadata.pagination.totalPages", is(1)))
		;
	}

	@Test
	public void testGetStudyDetails() throws Exception {
		// TODO with StudyResourceBrapi implementation
	}

}
