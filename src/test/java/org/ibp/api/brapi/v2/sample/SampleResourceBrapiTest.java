
package org.ibp.api.brapi.v2.sample;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.mockito.Mockito.doReturn;

public class SampleResourceBrapiTest extends ApiUnitTestBase {

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	@Autowired
	private SampleService sampleService;

	@Autowired
	private SearchRequestService searchRequestService;

	private SampleObservationDto getTestObservationDto() {
		final SampleObservationDto sampleObservationDto =
			new SampleObservationDto();
		sampleObservationDto.setAdditionalInfo(Collections.singletonMap(randomAlphanumeric(10), randomAlphanumeric(10)));
		final ExternalReferenceDTO extRef = new ExternalReferenceDTO();
		extRef.setReferenceID(randomAlphanumeric(20));
		extRef.setReferenceSource(randomAlphanumeric(10));
		sampleObservationDto.setExternalReferences(Collections.singletonList(extRef));

		sampleObservationDto.setColumn(6);
		sampleObservationDto.setGermplasmDbId(randomAlphanumeric(10));
		sampleObservationDto.setObservationUnitDbId(randomAlphanumeric(10));
		sampleObservationDto.setPlateDbId(randomAlphanumeric(10));
		sampleObservationDto.setPlateName(randomAlphanumeric(20));
		sampleObservationDto.setProgramDbId(randomAlphanumeric(10));
		sampleObservationDto.setRow(randomAlphanumeric(1));
		sampleObservationDto.setSampleBarcode(randomAlphanumeric(10));
		sampleObservationDto.setSampleDbId(randomAlphanumeric(10));
		sampleObservationDto.setSampleDescription("This sample was taken from the root of a tree");
		sampleObservationDto.setSampleGroupDbId(randomAlphanumeric(10));
		sampleObservationDto.setSampleName(randomAlphanumeric(20));
		sampleObservationDto.setSamplePUI(randomAlphanumeric(20));
		sampleObservationDto.setSampleTimestamp(new Date());
		sampleObservationDto.setSampleType(randomAlphanumeric(6));
		sampleObservationDto.setStudyDbId(randomNumeric(2));
		sampleObservationDto.setTakenBy(randomAlphanumeric(6));
		sampleObservationDto.setTissueType(randomAlphanumeric(6));
		sampleObservationDto.setTrialDbId(randomAlphanumeric(10));
		sampleObservationDto.setWell(randomAlphanumeric(2));

		return sampleObservationDto;
	}

	@Test
	public void testGetSampleSearchResults() throws Exception {
		final String sampleDbId = String.valueOf(nextInt());
		final int searchResultsDbid = 1;
		final SampleSearchRequestDTO sampleSearchRequest = new SampleSearchRequestDTO();
		sampleSearchRequest.setSampleDbIds(Collections.singletonList(sampleDbId));

		final SampleObservationDto sampleObservationDto = this.getTestObservationDto();
		final List<SampleObservationDto> sampleObservationDtoList = Collections.singletonList(sampleObservationDto);

		doReturn(sampleSearchRequest).when(this.searchRequestService).getSearchRequest(searchResultsDbid, SampleSearchRequestDTO.class);
		doReturn(sampleObservationDtoList).when(this.sampleService)
			.getSampleObservations(sampleSearchRequest,
				new PageRequest(BrapiPagedResult.DEFAULT_PAGE_NUMBER, BrapiPagedResult.DEFAULT_PAGE_SIZE));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v2/search/samples/" + searchResultsDbid)
				.contentType(this.contentType)
				.locale(Locale.getDefault()))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(sampleObservationDtoList.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.is(sampleObservationDto.getAdditionalInfo())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].externalReferences",
				IsCollectionWithSize.hasSize(sampleObservationDto.getExternalReferences().size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].externalReferences[0].referenceID",
				Matchers.is(sampleObservationDto.getExternalReferences().get(0).getReferenceID())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].externalReferences[0].referenceSource",
				Matchers.is(sampleObservationDto.getExternalReferences().get(0).getReferenceSource())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].studyDbId",
				Matchers.is(sampleObservationDto.getStudyDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].column",
				Matchers.is(sampleObservationDto.getColumn())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId",
				Matchers.is(sampleObservationDto.getGermplasmDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].observationUnitDbId",
				Matchers.is(sampleObservationDto.getObservationUnitDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].plateDbId",
				Matchers.is(sampleObservationDto.getPlateDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].plateName",
				Matchers.is(sampleObservationDto.getPlateName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId",
				Matchers.is(sampleObservationDto.getProgramDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].row",
				Matchers.is(sampleObservationDto.getRow())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].sampleBarcode",
				Matchers.is(sampleObservationDto.getSampleBarcode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].sampleDbId",
				Matchers.is(sampleObservationDto.getSampleDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].sampleDescription",
				Matchers.is(sampleObservationDto.getSampleDescription())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].sampleGroupDbId",
				Matchers.is(sampleObservationDto.getSampleGroupDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].sampleName",
				Matchers.is(sampleObservationDto.getSampleName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].samplePUI",
				Matchers.is(sampleObservationDto.getSamplePUI())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].sampleTimestamp",
				Matchers.is(SampleResourceBrapiTest.DATE_FORMAT.format(sampleObservationDto.getSampleTimestamp()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].sampleType",
				Matchers.is(sampleObservationDto.getSampleType())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].takenBy",
				Matchers.is(sampleObservationDto.getTakenBy())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].tissueType",
				Matchers.is(sampleObservationDto.getTissueType())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].trialDbId",
				Matchers.is(sampleObservationDto.getTrialDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].well",
				Matchers.is(sampleObservationDto.getWell())));
	}

	@Test
	public void testPostSearchGermplasm() throws Exception {

		final String sampleDbId = String.valueOf(nextInt());
		final Integer searchResultsDbId = 1;
		final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO();
		requestDTO.setSampleDbIds(Lists.newArrayList(sampleDbId));

		doReturn(searchResultsDbId).when(this.searchRequestService).saveSearchRequest(requestDTO, SampleSearchRequestDTO.class);

		this.mockMvc.perform(MockMvcRequestBuilders.post("/maize/brapi/v2/search/samples")
				.content(this.convertObjectToByte(requestDTO))
				.contentType(this.contentType)
				.locale(Locale.getDefault()))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.searchResultsDbId", Matchers.is(String.valueOf(searchResultsDbId))));

	}
}
