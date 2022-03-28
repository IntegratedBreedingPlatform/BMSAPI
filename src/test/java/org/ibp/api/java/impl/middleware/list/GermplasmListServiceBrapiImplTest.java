package org.ibp.api.java.impl.middleware.list;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.GermplasmListServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.list.GermplasmListImportRequestDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmListSearchRequestDTO;
import org.generationcp.middleware.service.api.GermplasmListDTO;
import org.ibp.api.brapi.v2.list.GermplasmListImportResponse;
import org.ibp.api.java.impl.middleware.list.validator.GermplasmListImportRequestValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListServiceBrapiImplTest {

	@Mock
	private GermplasmListServiceBrapi middlewareGermplasmListServiceBrapi;

	@Mock
	private GermplasmListImportRequestValidator germplasmListImportRequestValidator;

	@InjectMocks
	private GermplasmListServiceBrapiImpl germplasmListServiceBrapi;

	@Test
	public void testSearchGermplasmListDTOs() {
		final GermplasmListDTO listDTO =  new GermplasmListDTO();
		listDTO.setListDbId("1");
		listDTO.setListName(RandomStringUtils.randomAlphabetic(10));
		listDTO.setListDescription(RandomStringUtils.randomAlphabetic(10));
		Mockito.when(this.middlewareGermplasmListServiceBrapi.searchGermplasmListDTOs(Mockito.any(), ArgumentMatchers.isNull()))
			.thenReturn(Collections.singletonList(listDTO));

		final List<GermplasmListDTO> lists = this.germplasmListServiceBrapi
			.searchGermplasmListDTOs(new GermplasmListSearchRequestDTO(), null);
		Assert.assertEquals(1, lists.size());
		Assert.assertEquals(listDTO.getListDbId(), lists.get(0).getListDbId());
		Assert.assertEquals(listDTO.getListName(), lists.get(0).getListName());
		Assert.assertEquals(listDTO.getListDescription(), lists.get(0).getListDescription());
	}

	@Test
	public void testCountGermplasmListDTOs() {
		Mockito.when(this.middlewareGermplasmListServiceBrapi.countGermplasmListDTOs(Mockito.any()))
			.thenReturn(1l);

		final long count = this.germplasmListServiceBrapi.countGermplasmListDTOs(new GermplasmListSearchRequestDTO());
		Assert.assertEquals(1l, count);
	}

	@Test
	public void createGermplasmLists() {
		final List<GermplasmListImportRequestDTO> importRequestDTOS =  Collections.singletonList(new GermplasmListImportRequestDTO());

		final GermplasmListDTO listDTO =  new GermplasmListDTO();
		listDTO.setListDbId("1");
		listDTO.setListName(RandomStringUtils.randomAlphabetic(10));
		listDTO.setListDescription(RandomStringUtils.randomAlphabetic(10));
		Mockito.when(this.middlewareGermplasmListServiceBrapi.saveGermplasmListDTOs(importRequestDTOS))
			.thenReturn(Collections.singletonList(listDTO));

		Mockito.when( this.germplasmListImportRequestValidator.pruneListsInvalidForImport(importRequestDTOS))
			.thenReturn(new MapBindingResult(new HashMap<>(), Integer.class.getName()));

		final GermplasmListImportResponse response = this.germplasmListServiceBrapi.createGermplasmLists(importRequestDTOS);
		Assert.assertEquals(1, response.getCreatedSize().intValue());
		Assert.assertEquals(1, response.getImportListSize().intValue());
		Assert.assertEquals(1, response.getEntityList().size());
		final GermplasmListDTO createdList = response.getEntityList().get(0);
		Assert.assertEquals(listDTO.getListDbId(), createdList.getListDbId());
		Assert.assertEquals(listDTO.getListName(), createdList.getListName());
		Assert.assertEquals(listDTO.getListDescription(), createdList.getListDescription());
	}

}
