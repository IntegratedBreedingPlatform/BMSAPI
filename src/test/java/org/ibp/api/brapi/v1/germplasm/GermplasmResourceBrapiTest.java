package org.ibp.api.brapi.v1.germplasm;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.ParentType;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.search_request.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class GermplasmResourceBrapiTest extends ApiUnitTestBase {

	private static final Locale locale = Locale.getDefault();

	@Configuration
	public static class TestConfiguration {
		@Bean
		@Primary
		public GermplasmService germplasmService() {
			return Mockito.mock(GermplasmService.class);
		}
	}

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private GermplasmService germplasmService;

	@Test
	public void testGetPedigree() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);

		final PedigreeDTO pedigreeDTO = new PedigreeDTO();
		pedigreeDTO.setGermplasmDbId(gid);
		pedigreeDTO.setPedigree(randomAlphanumeric(255));

		doReturn(pedigreeDTO).when(this.germplasmService).getPedigree(gid, null, null);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/germplasm/" + germplasmDbId + "/pedigree")
			.contentType(this.contentType)
			.locale(locale))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(jsonPath("$.result.germplasmDbId", is(pedigreeDTO.getGermplasmDbId())))
			.andExpect(jsonPath("$.result.pedigree", is(pedigreeDTO.getPedigree())))
		;
	}

	@Test
	public void testGetProgeny() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);

		final ProgenyDTO progenyDTO = new ProgenyDTO();
		progenyDTO.setGermplasmDbId(gid);
		final List<ProgenyDTO.Progeny> progenies = new ArrayList<>();
		final ProgenyDTO.Progeny progeny = new ProgenyDTO.Progeny();
		progeny.setGermplasmDbId(nextInt());
		progeny.setParentType(ParentType.MALE.name());
		progenies.add(progeny);
		progenyDTO.setProgeny(progenies);

		doReturn(progenyDTO).when(this.germplasmService).getProgeny(gid);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/germplasm/" + germplasmDbId + "/progeny")
			.contentType(this.contentType)
			.locale(locale))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(jsonPath("$.result.germplasmDbId", is(gid)))
			.andExpect(jsonPath("$.result.progeny[0].germplasmDbId", is(progenyDTO.getProgeny().get(0).getGermplasmDbId())))
			.andExpect(jsonPath("$.result.progeny[0].parentType", is(progenyDTO.getProgeny().get(0).getParentType())))
		;
	}

	@Test
	public void testGetSearchGermplasm() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);
		final int searchResultsDbid = 1;
		final GermplasmSearchRequestDto germplasmSearchRequestDTO = new GermplasmSearchRequestDto();
		germplasmSearchRequestDTO.setGermplasmDbIds(Lists.newArrayList(germplasmDbId));
		final List<GermplasmDTO> list = new ArrayList<>();
		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(germplasmDbId);
		list.add(germplasmDTO);

		doReturn(germplasmSearchRequestDTO).when(this.searchRequestService).getSearchRequest(searchResultsDbid, GermplasmSearchRequestDto.class);
		doReturn(list).when(this.germplasmService)
			.searchGermplasmDTO(germplasmSearchRequestDTO, BrapiPagedResult.DEFAULT_PAGE_NUMBER, BrapiPagedResult.DEFAULT_PAGE_SIZE);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/search/germplasm/" + searchResultsDbid)
			.contentType(this.contentType)
			.locale(locale))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId",
				Matchers.is(germplasmDbId)));
	}

	@Test
	public void testSearchGermplasms() throws Exception {

		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);
		final List<GermplasmDTO> list = new ArrayList<>();
		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(germplasmDbId);
		list.add(germplasmDTO);

		when(this.germplasmService.countGermplasmDTOs(any(GermplasmSearchRequestDto.class))).thenReturn(1L);
		when(this.germplasmService
			.searchGermplasmDTO(any(GermplasmSearchRequestDto.class), Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_NUMBER),
				Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_SIZE)))
			.thenReturn(list);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/germplasm-search")
			.contentType(this.contentType)
			.locale(locale))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId",
				Matchers.is(germplasmDbId)));

	}
}
