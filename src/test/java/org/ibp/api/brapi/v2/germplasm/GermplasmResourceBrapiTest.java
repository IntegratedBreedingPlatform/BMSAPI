package org.ibp.api.brapi.v2.germplasm;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.mockito.Mockito.doReturn;

public class GermplasmResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private GermplasmService germplasmService;

	@Test
	public void testGetGermplasm() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);
		final GermplasmSearchRequestDto germplasmSearchRequestDTO = new GermplasmSearchRequestDto();
		germplasmSearchRequestDTO.setGermplasmDbIds(Lists.newArrayList(germplasmDbId));
		final List<GermplasmDTO> list = new ArrayList<>();
		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(germplasmDbId);
		germplasmDTO.setAccessionNumber(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setBreedingMethodDbId(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setCountryOfOriginCode(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGenus(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGermplasmName(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGermplasmOrigin(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGermplasmPUI(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setSeedSource(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setSynonyms(Lists.newArrayList(RandomStringUtils.randomAlphabetic(20)));
		final String attributeType = "PLOTCODE";
		germplasmDTO.setAdditionalInfo(Collections.singletonMap(attributeType, RandomStringUtils.randomAlphabetic(20)));
		list.add(germplasmDTO);

		doReturn(list).when(this.germplasmService)
			.searchGermplasmDTO(Mockito.any(GermplasmSearchRequestDto.class), Mockito
				.eq(new PageRequest(BrapiPagedResult.DEFAULT_PAGE_NUMBER, BrapiPagedResult.DEFAULT_PAGE_SIZE)));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v2/germplasm")
			.contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId",
				Matchers.is(germplasmDbId)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].accessionNumber",
				Matchers.is(germplasmDTO.getAccessionNumber())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].breedingMethodDbId",
				Matchers.is(germplasmDTO.getBreedingMethodDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryOfOriginCode",
				Matchers.is(germplasmDTO.getCountryOfOriginCode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].commonCropName",
				Matchers.is("maize")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].defaultDisplayName",
				Matchers.is(germplasmDTO.getDefaultDisplayName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].genus",
				Matchers.is(germplasmDTO.getGenus())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmName",
				Matchers.is(germplasmDTO.getGermplasmName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmOrigin",
				Matchers.is(germplasmDTO.getGermplasmOrigin())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmPUI",
				Matchers.is(germplasmDTO.getGermplasmPUI())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryOfOriginCode",
				Matchers.is(germplasmDTO.getCountryOfOriginCode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.hasKey(attributeType)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.hasValue(germplasmDTO.getAdditionalInfo().get(attributeType))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].synonyms[0]",
				Matchers.is(germplasmDTO.getSynonyms().get(0))));

	}

}
