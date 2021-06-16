package org.ibp.api.brapi.v1.study;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.study.StudyInstanceService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class StudyResourceBrapiTest extends ApiUnitTestBase {

	@SuppressWarnings("unused")
	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService studyServiceMW;

	@Autowired
	private StudyInstanceService studyInstanceService;

	@Test
	public void testListStudySummaries() throws Exception {

		// TODO with StudyResourceBrapi implementation
	}

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public LocationService locationService() {
			return Mockito.mock(LocationService.class);
		}
	}


	@Autowired
	private LocationService locationService;

	@Test
	public void testGetStudyObservationsAsTable() throws Exception {

		final int trialDbId = current().nextInt();
		final int studyDbId = current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();

		final List<List<String>> data = ImmutableList.<List<String>>builder().add(ImmutableList.<String>builder().add(randomAlphabetic(5))
			.add(randomAlphabetic(5)).add(randomAlphabetic(5)).add(randomAlphabetic(5)).build()).build();

		final TrialObservationTable observationTable =
			new TrialObservationTable().setStudyDbId(studyDbId).setObservationVariableDbIds(observationVariablesId)
				.setObservationVariableNames(observationVariableName).setData(data);

		Mockito.when(this.studyServiceMW.getTrialObservationTable(trialDbId, studyDbId)).thenReturn(observationTable);

		Mockito.when(this.studyDataManager.getProjectIdByStudyDbId(studyDbId)).thenReturn(trialDbId);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies/{studyDbId}/table")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build()).encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(jsonPath("$.result.studyDbId", is(observationTable.getStudyDbId())))
			.andExpect(jsonPath("$.result.observationVariableDbIds", IsCollectionWithSize.hasSize(observationVariablesId.size())))
			.andExpect(jsonPath("$.result.observationVariableDbIds[0]", is(observationVariablesId.get(0))))
			.andExpect(jsonPath("$.result.observationVariableNames", IsCollectionWithSize.hasSize(observationVariableName.size())))
			.andExpect(jsonPath("$.result.observationVariableNames[0]", is(observationVariableName.get(0))))
			.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(data.size())))
			.andExpect(jsonPath("$.result.data[0][0]", is(data.get(0).get(0))))
			.andExpect(jsonPath("$.result.data[0][1]", is(data.get(0).get(1))))
			.andExpect(jsonPath("$.result.data[0][2]", is(data.get(0).get(2))))
			.andExpect(jsonPath("$.result.data[0][3]", is(data.get(0).get(3))))
			.andExpect(jsonPath("$.metadata.pagination.currentPage", is(1)))
			.andExpect(jsonPath("$.metadata.pagination.pageSize", is(1)))
			.andExpect(jsonPath("$.metadata.pagination.totalCount", is(1)))
			.andExpect(jsonPath("$.metadata.pagination.totalPages", is(1)))
		;
	}

	@Test
	public void testGetStudyObservationsAsTableCSVRedirect() throws Exception {

		final int trialDbId = current().nextInt();
		final int studyDbId = current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();

		final List<List<String>> data = ImmutableList.<List<String>>builder().add(
			ImmutableList.<String>builder().add(randomAlphabetic(5)).add(randomAlphabetic(5)).add(randomAlphabetic(5))
				.add(randomAlphabetic(5)).build()).build();

		final TrialObservationTable observationTable =
			new TrialObservationTable().setStudyDbId(studyDbId).setObservationVariableDbIds(observationVariablesId)
				.setObservationVariableNames(observationVariableName).setData(data);

		Mockito.when(this.studyServiceMW.getTrialObservationTable(trialDbId, studyDbId)).thenReturn(observationTable);

		Mockito.when(this.studyDataManager.getProjectIdByStudyDbId(studyDbId)).thenReturn(trialDbId);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies/{studyDbId}/table?format=csv")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build());

		final UriComponents redirectedUriComponents =
			UriComponentsBuilder.newInstance().path("/bmsapi/maize/brapi/v1/studies/{studyDbId}/table/csv")
				.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build());

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.csvContentType))
			.andExpect(MockMvcResultMatchers.redirectedUrl(redirectedUriComponents.toUriString()))
		;
	}

	@Test
	public void testGetStudyObservationsAsTableTSVRedirect() throws Exception {

		final int trialDbId = current().nextInt();
		final int studyDbId = current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();

		final List<List<String>> data = ImmutableList.<List<String>>builder().add(
			ImmutableList.<String>builder().add(randomAlphabetic(5)).add(randomAlphabetic(5)).add(randomAlphabetic(5))
				.add(randomAlphabetic(5)).build()).build();

		final TrialObservationTable observationTable =
			new TrialObservationTable().setStudyDbId(studyDbId).setObservationVariableDbIds(observationVariablesId)
				.setObservationVariableNames(observationVariableName).setData(data);

		Mockito.when(this.studyServiceMW.getTrialObservationTable(trialDbId, studyDbId)).thenReturn(observationTable);

		Mockito.when(this.studyDataManager.getProjectIdByStudyDbId(studyDbId)).thenReturn(trialDbId);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies/{studyDbId}/table?format=tsv")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build());

		final UriComponents redirectedUriComponents =
			UriComponentsBuilder.newInstance().path("/bmsapi/maize/brapi/v1/studies/{studyDbId}/table/tsv")
				.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build());

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.csvContentType))
			.andExpect(MockMvcResultMatchers.redirectedUrl(redirectedUriComponents.toUriString()))
		;
	}

	@Test
	public void testGetStudyDetails() throws Exception {

		final StudyDetailsDto studyDetailsDto = StudyTestDataProvider.getStudyDetailsDto();
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationIds(Collections.singletonList(studyDetailsDto.getMetadata().getLocationId()));
		final List<Location> locations = StudyTestDataProvider.getLocationList();
		final Location location = locations.get(0);

		Mockito.when(this.studyInstanceService.getStudyDetailsByGeolocation(studyDetailsDto.getMetadata().getStudyDbId()))
			.thenReturn(studyDetailsDto);
		Mockito.when(this.locationService.getLocations(locationSearchRequest, new PageRequest(0, 10))).thenReturn(locations);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies/{studyDbId}")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDetailsDto.getMetadata().getStudyDbId()).build())
			.encode();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(
			jsonPath("$.result.studyDbId", is(studyDetailsDto.getMetadata().getStudyDbId().toString())))
			.andExpect(jsonPath("$.result.studyName", is(studyDetailsDto.getMetadata().getStudyName())))
			.andExpect(jsonPath("$.result.studyType", is(studyDetailsDto.getMetadata().getStudyType())))
			.andExpect(jsonPath("$.result.seasons", IsCollectionWithSize.hasSize(studyDetailsDto.getMetadata().getSeasons().size())))
			.andExpect(jsonPath("$.result.seasons[0]", is(studyDetailsDto.getMetadata().getSeasons().get(0))))
			.andExpect(jsonPath("$.result.trialDbId", is(studyDetailsDto.getMetadata().getTrialDbId().toString())))
			.andExpect(jsonPath("$.result.trialName", is(studyDetailsDto.getMetadata().getTrialName())))
			.andExpect(jsonPath("$.result.startDate", is(simpleDateFormat.format(studyDetailsDto.getMetadata().getStartDate()))))
			.andExpect(jsonPath("$.result.endDate", is(simpleDateFormat.format(studyDetailsDto.getMetadata().getEndDate()))))
			.andExpect(jsonPath("$.result.active", is(studyDetailsDto.getMetadata().getActive().toString())))
			.andExpect(jsonPath("$.result.location.locationDbId", is(location.getLocationDbId().toString())))
			.andExpect(jsonPath("$.result.location.locationType", is(location.getLocationType())))
			.andExpect(jsonPath("$.result.location.name", is(location.getName())))
			.andExpect(jsonPath("$.result.location.abbreviation", is(location.getAbbreviation())))
			.andExpect(jsonPath("$.result.location.countryCode", is(location.getCountryCode())))
			.andExpect(jsonPath("$.result.location.countryName", is(location.getCountryName())))
			.andExpect(jsonPath("$.result.location.latitude", is(location.getLatitude())))
			.andExpect(jsonPath("$.result.location.longitude", is(location.getLongitude())))
			.andExpect(jsonPath("$.result.location.altitude", is(location.getAltitude())))
			.andExpect(jsonPath("$.result.contacts[0].contactDbId", is(String.valueOf(studyDetailsDto.getContacts().get(0).getUserId()))))
			.andExpect(jsonPath("$.result.contacts[0].name",
				is(studyDetailsDto.getContacts().get(0).getFirstName() + " " + studyDetailsDto.getContacts().get(0).getLastName())))
			.andExpect(jsonPath("$.result.contacts[0].email", is(studyDetailsDto.getContacts().get(0).getEmail())))
			.andExpect(jsonPath("$.result.contacts[0].orcid", is("")))
			.andExpect(jsonPath("$.result.additionalInfo", hasKey("prop1")))
			.andExpect(jsonPath("$.result.additionalInfo", hasValue("val1")))
			.andExpect(jsonPath("$.metadata.pagination.currentPage", is(1)))
			.andExpect(jsonPath("$.metadata.pagination.pageSize", is(1)))
			.andExpect(jsonPath("$.metadata.pagination.totalCount", is(1)))
			.andExpect(jsonPath("$.metadata.pagination.totalPages", is(1)));

	}

	@Test
	public void testGetStudyObservationsAsTableCSV() throws Exception {

		final int trialDbId = current().nextInt();
		final int studyDbId = current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();

		final List<String> headerRow = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();
		;

		final List<List<String>> data = ImmutableList.<List<String>>builder().add(
			ImmutableList.<String>builder().add(randomAlphabetic(5)).add(randomAlphabetic(5)).add(randomAlphabetic(5))
				.add(randomAlphabetic(5)).build()).build();

		final TrialObservationTable observationTable =
			new TrialObservationTable().setStudyDbId(studyDbId).setObservationVariableDbIds(observationVariablesId)
				.setObservationVariableNames(observationVariableName).setData(data);
		observationTable.setHeaderRow(headerRow);

		Mockito.when(this.studyServiceMW.getTrialObservationTable(trialDbId, studyDbId)).thenReturn(observationTable);

		Mockito.when(this.studyDataManager.getProjectIdByStudyDbId(studyDbId)).thenReturn(trialDbId);

		String stringResult = headerRow.get(0) + "," + observationVariableName.get(0)
			+ "|" + observationVariablesId.get(0) + "\n";

		final Object[] dataArray = data.get(0).toArray();

		for (int i = 0; i < dataArray.length; i++) {
			stringResult = stringResult + dataArray[i] + ",";
		}

		stringResult = stringResult.substring(0, stringResult.length() - 1);
		stringResult = stringResult + "\n";

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies/{studyDbId}/table/csv")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build());

		final MvcResult result =
			this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.csvContentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(stringResult))
				.andReturn();

	}

	@Test
	public void testGetStudyObservationsAsTableTSV() throws Exception {

		final int trialDbId = current().nextInt();
		final int studyDbId = current().nextInt();

		final List<Integer> observationVariablesId = ImmutableList.<Integer>builder().add(current().nextInt()).build();

		final List<String> observationVariableName = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();

		final List<String> headerRow = ImmutableList.<String>builder().add(randomAlphabetic(5)).build();
		;

		final List<List<String>> data = ImmutableList.<List<String>>builder().add(
			ImmutableList.<String>builder().add(randomAlphabetic(5)).add(randomAlphabetic(5)).add(randomAlphabetic(5))
				.add(randomAlphabetic(5)).build()).build();

		final TrialObservationTable observationTable =
			new TrialObservationTable().setStudyDbId(studyDbId).setObservationVariableDbIds(observationVariablesId)
				.setObservationVariableNames(observationVariableName).setData(data);
		observationTable.setHeaderRow(headerRow);

		Mockito.when(this.studyServiceMW.getTrialObservationTable(trialDbId, studyDbId)).thenReturn(observationTable);

		Mockito.when(this.studyDataManager.getProjectIdByStudyDbId(studyDbId)).thenReturn(trialDbId);

		String stringResult = headerRow.get(0) + "\t" + observationVariableName.get(0)
			+ "|" + observationVariablesId.get(0) + "\n";

		final Object[] dataArray = data.get(0).toArray();

		for (int i = 0; i < dataArray.length; i++) {
			stringResult = stringResult + dataArray[i] + "\t";
		}

		stringResult = stringResult.substring(0, stringResult.length() - 1);
		stringResult = stringResult + "\n";

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies/{studyDbId}/table/tsv")
			.buildAndExpand(ImmutableMap.<String, Object>builder().put("studyDbId", studyDbId).build());

		final MvcResult result =
			this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.csvContentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(stringResult))
				.andReturn();

	}

	@Test
	public void testGetStudies() throws Exception {

		final List<StudyInstanceDto> studyInstanceDtos = StudyTestDataProvider.getListStudyDto();
		final StudyInstanceDto studyInstanceDto = studyInstanceDtos.get(0);

		Mockito.when(this.studyInstanceService.getStudyInstances(Mockito.any(StudySearchFilter.class), Mockito.any(PageRequest.class)))
			.thenReturn(studyInstanceDtos);
		Mockito.when(this.studyInstanceService.countStudyInstances(Mockito.any(StudySearchFilter.class))).thenReturn(1l);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/studies")
			.build().encode();

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toUriString()).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(jsonPath("$.result.data[0].active", is(studyInstanceDto.getActive())))
			.andExpect(jsonPath("$.result.data[0].commonCropName", is(studyInstanceDto.getCommonCropName())))
			.andExpect(jsonPath("$.result.data[0].startDate", is(simpleDateFormat.format(studyInstanceDto.getStartDate()))))
			.andExpect(jsonPath("$.result.data[0].endDate", is(simpleDateFormat.format(studyInstanceDto.getEndDate()))))
			.andExpect(jsonPath("$.result.data[0].studyDbId", is(studyInstanceDto.getStudyDbId())))
			.andExpect(jsonPath("$.result.data[0].studyName", is(studyInstanceDto.getStudyName())))
			.andExpect(jsonPath("$.result.data[0].studyType").doesNotExist())
			.andExpect(jsonPath("$.result.data[0].studyTypeDbId", is(studyInstanceDto.getStudyTypeDbId())))
			.andExpect(jsonPath("$.result.data[0].studyTypeName", is(studyInstanceDto.getStudyTypeName())))
			.andExpect(jsonPath("$.result.data[0].seasons[0].seasonDbId", is(studyInstanceDto.getSeasons().get(0).getSeasonDbId())))
			.andExpect(jsonPath("$.result.data[0].seasons[0].season", is(studyInstanceDto.getSeasons().get(0).getSeason())))
			.andExpect(jsonPath("$.result.data[0].locationDbId", is(studyInstanceDto.getLocationDbId())))
			.andExpect(jsonPath("$.result.data[0].locationName", is(studyInstanceDto.getLocationName())))
			.andExpect(jsonPath("$.result.data[0].programDbId", is(studyInstanceDto.getProgramDbId())))
			.andExpect(jsonPath("$.result.data[0].programName", is(studyInstanceDto.getProgramName())))
			.andExpect(jsonPath("$.result.data[0].trialName", is(studyInstanceDto.getTrialName())))
			.andExpect(jsonPath("$.result.data[0].trialDbId", is(studyInstanceDto.getTrialDbId())))
			.andExpect(jsonPath("$.result.data[0].optionalInfo").doesNotExist());

	}

}
