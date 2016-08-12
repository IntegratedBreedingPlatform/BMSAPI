package org.ibp.api.brapi.v1.study;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyDetailDto;
import org.generationcp.middleware.service.api.study.StudyService;
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
import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class StudyResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService studyServiceMW;

	@Test
	public void testListStudySummaries() throws Exception {

		final int studyDbId = current().nextInt();
		final String locationId = randomAlphabetic(5);
		final String programDbId = randomAlphanumeric(5);
		final String name = randomAlphabetic(5);
		final String season = randomAlphabetic(5);
		final String type = randomAlphabetic(5);
		final String year = randomAlphabetic(5);
		final String optionalInfoKey = randomAlphabetic(5);
		final String optionalInfoValue = randomAlphabetic(5);

		final StudySummary studySummary = new StudySummary().setName(name)
				.setSeasons(ImmutableList.<String>builder().add(season).build()).setStudyDbid(studyDbId)
				.setLocationId(locationId).setProgramDbId(programDbId).setType(type)
				.setYears(ImmutableList.<String>builder().add(year).build()).setOptionalInfo(
						ImmutableMap.<String, String>builder().put(optionalInfoKey, optionalInfoValue).build());

		List<StudySummary> mwStudySummary = Lists.newArrayList(studySummary);
		Mockito.when(this.studyDataManager.findPagedProjects(programDbId, String.valueOf(locationId), season, 10, 1))
				.thenReturn(mwStudySummary);
		Mockito.when(this.studyDataManager.countAllStudies(anyString(), anyString(), anyString())).thenReturn(200L);

		UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies")
				.queryParam("programDbId", programDbId).queryParam("seasonDbId", season).queryParam("pageSize", 10)
				.queryParam("pageNumber", 1).queryParam("locationDbId", String.valueOf(locationId)).build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwStudySummary.size()))) //
				.andExpect(jsonPath("$.result.data[0].studyDbId", is(studySummary.getStudyDbid()))) //
				.andExpect(jsonPath("$.result.data[0].name", is(studySummary.getName()))) //
				.andExpect(jsonPath("$.result.data[0].studyType", is(studySummary.getType()))) //
				.andExpect(jsonPath("$.result.data[0].locationDbId", is(studySummary.getLocationId()))) //
				.andExpect(jsonPath("$.result.data[0].programDbId", is(studySummary.getProgramDbId()))) //
				.andExpect(jsonPath("$.result.data[0].years",
						IsCollectionWithSize.hasSize(studySummary.getYears().size()))) //
				.andExpect(jsonPath("$.result.data[0].years[0]", is(studySummary.getYears().get(0)))) //
				.andExpect(jsonPath("$.result.data[0].seasons",
						IsCollectionWithSize.hasSize(studySummary.getSeasons().size()))) //
				.andExpect(jsonPath("$.result.data[0].seasons[0]", is(studySummary.getSeasons().get(0)))) //
				.andExpect(jsonPath("$.result.data[0].optionalInfo", hasKey(optionalInfoKey)))
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.pageSize", is(10))) //
				.andExpect(jsonPath("$.metadata.pagination.totalCount", is(200))) //
				.andExpect(jsonPath("$.metadata.pagination.totalPages", is(20))) //
				.andExpect(jsonPath("$.metadata.status", is(IsCollectionWithSize.hasSize(0)))) //
		;

	}

	@Test
	public void testListStudyDetailsAsTableNotNullResults() throws Exception {

		final int studyDbId = current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();

		final List<List<String>> data = ImmutableList.<List<String>>builder()
				.add(ImmutableList.<String>builder().add(randomAlphabetic(5)).add(randomAlphabetic(5))
						.add(randomAlphabetic(5)).add(randomAlphabetic(5)).build())
				.build();

		StudyDetailDto mwStudyDetailDto = new StudyDetailDto().setStudyDbId(studyDbId)
				.setObservationVariableDbIds(observationVariablesId).setObservationVariableNames(observationVariableName)
				.setData(data);

		Mockito.when(this.studyServiceMW.getStudyDetails(studyDbId)).thenReturn(mwStudyDetailDto);

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.path("/maize/brapi/v1/studies/{studyDbId}/table")
				.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build()).encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print())
				.andExpect(jsonPath("$.result.studyDbId", is(mwStudyDetailDto.getStudyDbId()))) //
				.andExpect(jsonPath("$.result.observationVariableDbIds",
						IsCollectionWithSize.hasSize(observationVariablesId.size()))) //
				.andExpect(jsonPath("$.result.observationVariableDbIds[0]", is(observationVariablesId.get(0)))) //
				.andExpect(jsonPath("$.result.observationVariableNames",
						IsCollectionWithSize.hasSize(observationVariableName.size()))) //
				.andExpect(jsonPath("$.result.observationVariableNames[0]", is(observationVariableName.get(0)))) //
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(data.size()))) //
				.andExpect(jsonPath("$.result.data[0][0]", is(data.get(0).get(0)))) //
				.andExpect(jsonPath("$.result.data[0][1]", is(data.get(0).get(1)))) //
				.andExpect(jsonPath("$.result.data[0][2]", is(data.get(0).get(2)))) //
				.andExpect(jsonPath("$.result.data[0][3]", is(data.get(0).get(3)))) //
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.pageSize", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.totalCount", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.totalPages", is(1))) //
				.andExpect(jsonPath("$.metadata.status", is(IsCollectionWithSize.hasSize(0))));
	}

	@Test
	public void testListStudyDetailsAsTableNullResults() throws Exception {

		final int studyDbId = current().nextInt();

		StudyDetailDto mwStudyDetailDto = new StudyDetailDto().setStudyDbId(studyDbId)
				.setObservationVariableDbIds(Lists.<Integer>newArrayList()).setObservationVariableNames(Lists.<String>newArrayList())
				.setData(Lists.<List<String>>newArrayList());

		Mockito.when(this.studyServiceMW.getStudyDetails(studyDbId)).thenReturn(mwStudyDetailDto);

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.path("/maize/brapi/v1/studies/{studyDbId}/table")
				.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build()).encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print())
				.andExpect(jsonPath("$.result.studyDbId", is(mwStudyDetailDto.getStudyDbId()))) //
				.andExpect(jsonPath("$.result.observationVariableDbIds", IsCollectionWithSize.hasSize(0))) //
				.andExpect(jsonPath("$.result.observationVariableNames", IsCollectionWithSize.hasSize(0))) //
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(0))) //
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.pageSize", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.totalCount", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.totalPages", is(1))) //
				.andExpect(jsonPath("$.metadata.status", is(IsCollectionWithSize.hasSize(0))));
	}

}
