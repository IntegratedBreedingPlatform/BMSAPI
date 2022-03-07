
package org.ibp.api.brapi.v2.attribute;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueDto;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeValueSearchRequestDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.AttributeValueService;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.Mockito.doReturn;

public class AttributeValueResourceBrapiTest extends ApiUnitTestBase {

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static final String PROGRAM_UUID = "50a7e02e-db60-4240-bd64-417b34606e46";

	@Autowired
	private AttributeValueService attributeValueService;

	@Autowired
	private SearchRequestService searchRequestService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public AttributeValueService attributeValueService() {
			return Mockito.mock(AttributeValueService.class);
		}
	}

	private AttributeValueDto getTestAttributeValueDto() {
		final AttributeValueDto attributeValueDto =
			new AttributeValueDto();
		attributeValueDto.setAdditionalInfo(Collections.singletonMap(randomAlphanumeric(10), randomAlphanumeric(10)));
		final ExternalReferenceDTO extRef = new ExternalReferenceDTO();
		extRef.setReferenceID(randomAlphanumeric(20));
		extRef.setReferenceSource(randomAlphanumeric(10));
		attributeValueDto.setExternalReferences(Collections.singletonList(extRef));

		attributeValueDto.setAttributeValueDbId(randomAlphanumeric(10));
		attributeValueDto.setGermplasmDbId(randomAlphanumeric(10));
		attributeValueDto.setDeterminedDate(new Date());
		attributeValueDto.setAttributeDbId(randomAlphanumeric(10));
		attributeValueDto.setValue(randomAlphanumeric(10));
		attributeValueDto.setAttributeName(randomAlphanumeric(10));

		return attributeValueDto;
	}

	@Test
	public void testGetAttributeValueSearchResults() throws Exception {
		final String attributeValueId = String.valueOf(nextInt());
		final int searchResultsDbid = 1;
		final AttributeValueSearchRequestDto searchRequestDto = new AttributeValueSearchRequestDto();
		searchRequestDto.setAttributeValueDbIds(Collections.singletonList(attributeValueId));

		final AttributeValueDto attributeValueDto = this.getTestAttributeValueDto();
		final List<AttributeValueDto> attributeValueDtoList = Collections.singletonList(attributeValueDto);

		doReturn(searchRequestDto).when(this.searchRequestService)
			.getSearchRequest(searchResultsDbid, AttributeValueSearchRequestDto.class);
		doReturn(attributeValueDtoList).when(this.attributeValueService)
			.getAttributeValues(ArgumentMatchers.any(AttributeValueSearchRequestDto.class), ArgumentMatchers.any(
				Pageable.class), ArgumentMatchers.isNull());
		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v2/search/attributevalues/" + searchResultsDbid)
				.contentType(this.contentType)
				.locale(Locale.getDefault()))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(attributeValueDtoList.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.is(attributeValueDto.getAdditionalInfo())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].externalReferences",
				IsCollectionWithSize.hasSize(attributeValueDto.getExternalReferences().size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].externalReferences[0].referenceID",
				Matchers.is(attributeValueDto.getExternalReferences().get(0).getReferenceID())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].externalReferences[0].referenceSource",
				Matchers.is(attributeValueDto.getExternalReferences().get(0).getReferenceSource())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].attributeValueDbId",
				Matchers.is(attributeValueDto.getAttributeValueDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].attributeDbId",
				Matchers.is(attributeValueDto.getAttributeDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId",
				Matchers.is(attributeValueDto.getGermplasmDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].attributeName",
				Matchers.is(attributeValueDto.getAttributeName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmName",
				Matchers.is(attributeValueDto.getGermplasmName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].value",
				Matchers.is(attributeValueDto.getValue())));
	}

	@Test
	public void testPostAttributeValueSearch() throws Exception {

		final String attributeValueId = String.valueOf(nextInt());
		final Integer searchResultsDbId = 1;
		final AttributeValueSearchRequestDto requestDTO = new AttributeValueSearchRequestDto();
		requestDTO.setAttributeDbIds(Lists.newArrayList(attributeValueId));

		doReturn(searchResultsDbId).when(this.searchRequestService).saveSearchRequest(requestDTO, AttributeValueSearchRequestDto.class);

		this.mockMvc.perform(MockMvcRequestBuilders.post("/maize/brapi/v2/search/attributevalues")
				.content(this.convertObjectToByte(requestDTO))
				.contentType(this.contentType)
				.locale(Locale.getDefault()))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.searchResultsDbId", Matchers.is(String.valueOf(searchResultsDbId))));

	}

}
