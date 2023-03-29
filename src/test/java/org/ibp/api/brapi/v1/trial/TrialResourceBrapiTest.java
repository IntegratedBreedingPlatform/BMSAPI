
package org.ibp.api.brapi.v1.trial;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.TrialSummary;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.generationcp.middleware.service.api.user.ContactDto;
import org.generationcp.middleware.util.Util;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.TrialServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TrialResourceBrapiTest extends ApiUnitTestBase {

	private static final String MAIZE_BRAPI_V1_TRIALS = "/maize/brapi/v1/trials";
	private static final String MAIZE_BRAPI_V2_TRIALS = "/maize/brapi/v2/trials";
	@Autowired
	private TrialServiceBrapi trialServiceBrapi;

	@Test
	public void testListStudySummaries() throws Exception {

		final int studyDbId = ThreadLocalRandom.current().nextInt();
		final String locationId = RandomStringUtils.randomAlphabetic(5);
		final String programDbId = RandomStringUtils.randomAlphanumeric(5);
		final String programName = RandomStringUtils.randomAlphanumeric(5);
		final String startDate = "20160101";
		final String endDate = "20161201";
		final String name = RandomStringUtils.randomAlphabetic(5);
		final String season = RandomStringUtils.randomAlphabetic(5);
		final String type = RandomStringUtils.randomAlphabetic(5);
		final String year = RandomStringUtils.randomAlphabetic(5);
		final String additionalInfoKey = RandomStringUtils.randomAlphabetic(5);
		final String additionalInfoValue = RandomStringUtils.randomAlphabetic(5);

		final TrialSummary trialSummary = new TrialSummary();
		trialSummary.setName(name);
		trialSummary.setTrialDbId(studyDbId);
		trialSummary.setLocationId(locationId);
		trialSummary.setProgramDbId(programDbId);
		trialSummary.setProgramName(programName);
		trialSummary.setStartDate(DateUtil.parseDate(startDate));
		trialSummary.setEndDate(DateUtil.parseDate(endDate));
		trialSummary.setActive(true);
		trialSummary.setAdditionalInfo(ImmutableMap.<String, String>builder().put(additionalInfoKey, additionalInfoValue).build());

		final InstanceMetadata instanceMetadata = new InstanceMetadata();
		instanceMetadata.setInstanceDbId(1234);
		instanceMetadata.setTrialName(name);
		instanceMetadata.setInstanceNumber("111");
		instanceMetadata.setLocationName("INDIA");
		trialSummary.setInstanceMetaData(Lists.newArrayList(instanceMetadata));

		final List<TrialSummary> mwTrialSummary = Lists.newArrayList(trialSummary);
		Mockito.when(this.trialServiceBrapi.searchTrials(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mwTrialSummary);
		Mockito.when(this.trialServiceBrapi.countSearchTrialsResult(ArgumentMatchers.any())).thenReturn(200L);

		final int pageSize = 10;
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(TrialResourceBrapiTest.MAIZE_BRAPI_V1_TRIALS)
			.queryParam("programDbId", programDbId).build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwTrialSummary.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialDbId", Matchers.is(String.valueOf(trialSummary.getTrialDbId())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialName", Matchers.is(trialSummary.getName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId", Matchers.is(trialSummary.getProgramDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programName", Matchers.is(trialSummary.getProgramName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].startDate",
				Matchers.is(Util.formatDateAsStringValue(trialSummary.getStartDate(), Util.FRONTEND_DATE_FORMAT)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].endDate",
				Matchers.is(Util.formatDateAsStringValue(trialSummary.getEndDate(), Util.FRONTEND_DATE_FORMAT)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].active", Matchers.is(trialSummary.isActive()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies", Matchers.is(IsCollectionWithSize.hasSize(1)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies[0].studyDbId",
				Matchers.is(instanceMetadata.getInstanceDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies[0].studyName",
				Matchers.is(name + " Environment Number " + instanceMetadata.getInstanceNumber()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies[0].locationName",
				Matchers.is(instanceMetadata.getLocationName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo", Matchers.hasKey(additionalInfoKey)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo", Matchers.hasValue(additionalInfoValue)))
			// Default starting page index is 0
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage",
				Matchers.is(BrapiPagedResult.DEFAULT_PAGE_NUMBER)))
			// Default page size is 1000
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(BrapiPagedResult.DEFAULT_PAGE_SIZE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(200))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1)));
	}

	@Test
	public void testListStudySummariesWithPageParameters() throws Exception {

		final int studyDbId = ThreadLocalRandom.current().nextInt();
		final String locationId = RandomStringUtils.randomAlphabetic(5);
		final String programDbId = RandomStringUtils.randomAlphanumeric(5);
		final String programName = RandomStringUtils.randomAlphanumeric(5);
		final String startDate = "20160101";
		final String endDate = "20161201";
		final String name = RandomStringUtils.randomAlphabetic(5);
		final String season = RandomStringUtils.randomAlphabetic(5);
		final String type = RandomStringUtils.randomAlphabetic(5);
		final String year = RandomStringUtils.randomAlphabetic(5);
		final String additionalInfoKey = RandomStringUtils.randomAlphabetic(5);
		final String additionalInfoValue = RandomStringUtils.randomAlphabetic(5);

		final TrialSummary trialSummary = new TrialSummary();
		trialSummary.setName(name);
		trialSummary.setTrialDbId(studyDbId);
		trialSummary.setLocationId(locationId);
		trialSummary.setProgramDbId(programDbId);
		trialSummary.setProgramName(programName);
		trialSummary.setStartDate(DateUtil.parseDate(startDate));
		trialSummary.setEndDate(DateUtil.parseDate(endDate));
		trialSummary.setActive(true);
		trialSummary.setAdditionalInfo(ImmutableMap.<String, String>builder().put(additionalInfoKey, additionalInfoValue).build());

		final InstanceMetadata instanceMetadata = new InstanceMetadata();
		instanceMetadata.setInstanceDbId(1234);
		instanceMetadata.setTrialName(name);
		instanceMetadata.setInstanceNumber("111");
		instanceMetadata.setLocationName("INDIA");
		trialSummary.setInstanceMetaData(Lists.newArrayList(instanceMetadata));

		final List<TrialSummary> mwTrialSummary = Lists.newArrayList(trialSummary);
		final int count = 200;
		Mockito.when(this.trialServiceBrapi.searchTrials(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mwTrialSummary);
		Mockito.when(this.trialServiceBrapi.countSearchTrialsResult(ArgumentMatchers.any())).thenReturn(new Long(count));

		final int page = 1;
		final int pageSize = 10;
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(TrialResourceBrapiTest.MAIZE_BRAPI_V1_TRIALS)
			.queryParam("programDbId", programDbId).queryParam("page", page).queryParam("pageSize", pageSize).build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwTrialSummary.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialDbId", Matchers.is(String.valueOf(trialSummary.getTrialDbId())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialName", Matchers.is(trialSummary.getName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId", Matchers.is(trialSummary.getProgramDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programName", Matchers.is(trialSummary.getProgramName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].startDate",
				Matchers.is(Util.formatDateAsStringValue(trialSummary.getStartDate(), Util.FRONTEND_DATE_FORMAT)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].endDate",
				Matchers.is(Util.formatDateAsStringValue(trialSummary.getEndDate(), Util.FRONTEND_DATE_FORMAT)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].active", Matchers.is(trialSummary.isActive()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies", Matchers.is(IsCollectionWithSize.hasSize(1)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies[0].studyDbId",
				Matchers.is(instanceMetadata.getInstanceDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies[0].studyName",
				Matchers.is(name + " Environment Number " + instanceMetadata.getInstanceNumber()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studies[0].locationName",
				Matchers.is(instanceMetadata.getLocationName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo", Matchers.hasKey(additionalInfoKey)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo", Matchers.hasValue(additionalInfoValue)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage", Matchers.is(page)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(pageSize))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(count))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(count / pageSize)));
	}

	@Test
	public void testGetStudyObservationsAsTableNotNullResults() throws Exception {

		final int trialDbId = ThreadLocalRandom.current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(ThreadLocalRandom.current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(RandomStringUtils.randomAlphabetic(5)).build();

		final List<List<String>> data =
			ImmutableList.<List<String>>builder()
				.add(ImmutableList.<String>builder().add(RandomStringUtils.randomAlphabetic(5))
					.add(RandomStringUtils.randomAlphabetic(5)).add(RandomStringUtils.randomAlphabetic(5))
					.add(RandomStringUtils.randomAlphabetic(5)).build())
				.build();

		final TrialObservationTable mwStudyObservationTable = new TrialObservationTable().setStudyDbId(trialDbId)
			.setObservationVariableDbIds(observationVariablesId).setObservationVariableNames(observationVariableName).setData(data);

		Mockito.when(this.trialServiceBrapi.getTrialObservationTable(trialDbId)).thenReturn(mwStudyObservationTable);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/trials/{trialDbId}/table")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("trialDbId", trialDbId).build()).encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.trialDbId", Matchers.is(mwStudyObservationTable.getStudyDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.observationVariableDbIds",
				IsCollectionWithSize.hasSize(observationVariablesId.size()))) //
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.result.observationVariableDbIds[0]", Matchers.is(observationVariablesId.get(0)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.observationVariableNames",
				IsCollectionWithSize.hasSize(observationVariableName.size()))) //
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.result.observationVariableNames[0]", Matchers.is(observationVariableName.get(0)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(data.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0][0]", Matchers.is(data.get(0).get(0)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0][1]", Matchers.is(data.get(0).get(1)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0][2]", Matchers.is(data.get(0).get(2)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0][3]", Matchers.is(data.get(0).get(3)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1)));
	}

	@Test
	public void testGetStudyObservationsAsTableNullResults() throws Exception {

		final int trialDbId = ThreadLocalRandom.current().nextInt();

		final TrialObservationTable mwStudyObservationTable =
			new TrialObservationTable().setStudyDbId(trialDbId).setObservationVariableDbIds(Lists.<Integer>newArrayList())
				.setObservationVariableNames(Lists.<String>newArrayList()).setData(Lists.<List<String>>newArrayList());

		Mockito.when(this.trialServiceBrapi.getTrialObservationTable(trialDbId)).thenReturn(mwStudyObservationTable);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/trials/{trialDbId}/table")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("trialDbId", trialDbId).build()).encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.trialDbId", Matchers.is(mwStudyObservationTable.getStudyDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.observationVariableDbIds", IsCollectionWithSize.hasSize(0))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.observationVariableNames", IsCollectionWithSize.hasSize(0))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(0))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(1))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1))) //
		;
	}

	@Test
	public void testGetListStudySummariesBadOrderByFields() throws Exception {
		final UriComponents uriComponents =
			UriComponentsBuilder.newInstance().path(TrialResourceBrapiTest.MAIZE_BRAPI_V1_TRIALS).queryParam("programDbId", 1)
				.queryParam("pageSize", 10).queryParam("page", 1).queryParam("sortBy", "invalid_sort_By").build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotFound()) //
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0].message",
			Matchers.is("sortBy bad filter, expect trialDbId/trialName/programDbId/programName/startDate/endDate/active")));
	}


	@Test
	public void testGetListStudySummariesBadSorterOrder() throws Exception {
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(TrialResourceBrapiTest.MAIZE_BRAPI_V1_TRIALS)
			.queryParam("programDbId", 1).queryParam("pageSize", 10).queryParam("pageNumber", 1)
			.queryParam("sortOrder", "invalid_sort_order").build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotFound()) //
			.andDo(MockMvcResultHandlers.print()).andExpect(
			MockMvcResultMatchers.jsonPath("$.metadata.status[0].message", Matchers.is("sortOrder bad filter, expect asc/desc")));
	}

	@Test
	public void testListStudySummaries_V2() throws Exception {

		final int studyDbId = ThreadLocalRandom.current().nextInt();
		final String locationId = RandomStringUtils.randomAlphabetic(5);
		final String programDbId = RandomStringUtils.randomAlphanumeric(5);
		final String programName = RandomStringUtils.randomAlphanumeric(5);
		final String startDate = "20160101";
		final String endDate = "20161201";
		final String name = RandomStringUtils.randomAlphabetic(5);
		final String season = RandomStringUtils.randomAlphabetic(5);
		final String type = RandomStringUtils.randomAlphabetic(5);
		final String year = RandomStringUtils.randomAlphabetic(5);
		final String description = RandomStringUtils.randomAlphabetic(10);
		final String trialPUI = RandomStringUtils.randomAlphabetic(20);
		final String additionalInfoKey = RandomStringUtils.randomAlphabetic(5);
		final String additionalInfoValue = RandomStringUtils.randomAlphabetic(5);

		final ContactDto contactDto = new ContactDto(RandomStringUtils.randomNumeric(3), "Maize Breeder", "admin@abc.org", "Creator");

		final TrialSummary trialSummary = new TrialSummary();
		trialSummary.setName(name);
		trialSummary.setDescription(description);
		trialSummary.setTrialDbId(studyDbId);
		trialSummary.setLocationId(locationId);
		trialSummary.setProgramDbId(programDbId);
		trialSummary.setProgramName(programName);
		trialSummary.setStartDate(DateUtil.parseDate(startDate));
		trialSummary.setEndDate(DateUtil.parseDate(endDate));
		trialSummary.setActive(true);
		trialSummary.setObservationUnitId(trialPUI);
		trialSummary.setContacts(Collections.singletonList(contactDto));
		trialSummary.setAdditionalInfo(ImmutableMap.<String, String>builder().put(additionalInfoKey, additionalInfoValue).build());

		final InstanceMetadata instanceMetadata = new InstanceMetadata();
		instanceMetadata.setInstanceDbId(1234);
		instanceMetadata.setTrialName(name);
		instanceMetadata.setInstanceNumber("111");
		instanceMetadata.setLocationName("INDIA");
		trialSummary.setInstanceMetaData(Lists.newArrayList(instanceMetadata));

		final List<TrialSummary> mwTrialSummary = Lists.newArrayList(trialSummary);
		Mockito.when(this.trialServiceBrapi.searchTrials(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mwTrialSummary);
		Mockito.when(this.trialServiceBrapi.countSearchTrialsResult(ArgumentMatchers.any())).thenReturn(200L);

		final int pageSize = 10;
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(TrialResourceBrapiTest.MAIZE_BRAPI_V2_TRIALS)
			.queryParam("programDbId", programDbId).build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(mwTrialSummary.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialDbId", Matchers.is(String.valueOf(trialSummary.getTrialDbId())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialName", Matchers.is(trialSummary.getName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialDescription", Matchers.is(trialSummary.getDescription()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialPUI", Matchers.is(trialSummary.getObservationUnitId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId", Matchers.is(trialSummary.getProgramDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programName", Matchers.is(trialSummary.getProgramName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].startDate",
				Matchers.is(Util.formatDateAsStringValue(trialSummary.getStartDate(), Util.FRONTEND_DATE_FORMAT)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].endDate",
				Matchers.is(Util.formatDateAsStringValue(trialSummary.getEndDate(), Util.FRONTEND_DATE_FORMAT)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].active", Matchers.is(trialSummary.isActive()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo", Matchers.hasKey(additionalInfoKey)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo", Matchers.hasValue(additionalInfoValue)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].datasetAuthorships", Matchers.is(IsCollectionWithSize.hasSize(0)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].externalReferences", Matchers.is(IsCollectionWithSize.hasSize(0)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].publications", Matchers.is(IsCollectionWithSize.hasSize(0))))//
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].documentationURL", Matchers.is(""))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].contacts", Matchers.is(IsCollectionWithSize.hasSize(1)))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].contacts[0].contactDbId",
				Matchers.is(contactDto.getContactDbId()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].contacts[0].name",
				Matchers.is(contactDto.getName()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].contacts[0].email",
				Matchers.is(contactDto.getEmail()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].contacts[0].type",
				Matchers.is(contactDto.getType()))) //
			// Default starting page index is 0
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage",
				Matchers.is(BrapiPagedResult.DEFAULT_PAGE_NUMBER)))
			// Default page size is 1000
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(BrapiPagedResult.DEFAULT_PAGE_SIZE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(200))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1)));
	}
}
