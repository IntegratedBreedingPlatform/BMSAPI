package org.ibp.api.brapi.v1.trial;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.manager.api.StudyDataManager;
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

public class TrialResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private StudyDataManager studyDataManager;

	@Test
	public void testListTrialSummaries() throws Exception {

		final int studyDbId = current().nextInt();
		final String locationId = randomAlphabetic(5);
		final String programDbId = randomAlphanumeric(5);
		final String programName = randomAlphanumeric(5);
		final String startDate = "20160101";
		final String endDate = "20161201";
		final String name = randomAlphabetic(5);
		final String season = randomAlphabetic(5);
		final String type = randomAlphabetic(5);
		final String year = randomAlphabetic(5);
		final String additionalInfoKey = randomAlphabetic(5);
		final String additionalInfoValue = randomAlphabetic(5);

		final StudySummary studySummary = new StudySummary().setName(name).setSeasons(ImmutableList.<String>builder().add(season).build())
				.setStudyDbid(studyDbId).setLocationId(locationId).setProgramDbId(programDbId).setType(type)
				.setYears(ImmutableList.<String>builder().add(year).build())
				.setProgramDbId(programDbId).setProgramName(programName).setStartDate(startDate).setEndDate(endDate).setActive(true)
				.setOptionalInfo(ImmutableMap.<String, String>builder().put(additionalInfoKey, additionalInfoValue).build());
		
		final InstanceMetadata instanceMetadata = new InstanceMetadata();
		instanceMetadata.setInstanceDbId(1234);
		instanceMetadata.setTrialName(name);
		instanceMetadata.setInstanceNumber("111");
		instanceMetadata.setLocationName("INDIA");
		studySummary.setInstanceMetaData(Lists.newArrayList(instanceMetadata));

		List<StudySummary> mwStudySummary = Lists.newArrayList(studySummary);
		Mockito.when(this.studyDataManager.findPagedProjects(programDbId, String.valueOf(locationId), season, 10, 1))
				.thenReturn(mwStudySummary);
		Mockito.when(this.studyDataManager.countAllStudies(anyString(), anyString(), anyString())).thenReturn(200L);

		UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/trials")
				.queryParam("programDbId", programDbId).queryParam("seasonDbId", season).queryParam("pageSize", 10)
				.queryParam("pageNumber", 1).queryParam("locationDbId", String.valueOf(locationId)).build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwStudySummary.size()))) //
				.andExpect(jsonPath("$.result.data[0].trialDbId", is(studySummary.getStudyDbid()))) //
				.andExpect(jsonPath("$.result.data[0].trialName", is(studySummary.getName()))) //
				.andExpect(jsonPath("$.result.data[0].programDbId", is(studySummary.getProgramDbId()))) //
				.andExpect(jsonPath("$.result.data[0].programName", is(studySummary.getProgramName()))) //
				.andExpect(jsonPath("$.result.data[0].startDate", is(studySummary.getStartDate()))) //
				.andExpect(jsonPath("$.result.data[0].endDate", is(studySummary.getEndDate()))) //
				.andExpect(jsonPath("$.result.data[0].active", is(studySummary.isActive()))) //
				.andExpect(jsonPath("$.result.data[0].studies", is(IsCollectionWithSize.hasSize(1)))) //
				.andExpect(jsonPath("$.result.data[0].studies[0].studyDbId", is(instanceMetadata.getInstanceDbId()))) //
				.andExpect(jsonPath("$.result.data[0].studies[0].studyName",
						is(name + " Environment Number " + instanceMetadata.getInstanceNumber()))) //
				.andExpect(jsonPath("$.result.data[0].studies[0].locationName", is(instanceMetadata.getLocationName()))) //
				.andExpect(jsonPath("$.result.data[0].additionalInfo", hasKey(additionalInfoKey)))
				.andExpect(jsonPath("$.result.data[0].additionalInfo", hasValue(additionalInfoValue)))
				.andExpect(jsonPath("$.metadata.pagination.pageNumber", is(1))) //
				.andExpect(jsonPath("$.metadata.pagination.pageSize", is(10))) //
				.andExpect(jsonPath("$.metadata.pagination.totalCount", is(200))) //
				.andExpect(jsonPath("$.metadata.pagination.totalPages", is(20))) //
				.andExpect(jsonPath("$.metadata.status", is(IsCollectionWithSize.hasSize(0)))) //
		;

	}
}
