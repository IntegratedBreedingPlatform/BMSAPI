
package org.ibp.api.java.impl.middleware.germplasm;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.service.api.PedigreeService;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmDeleteValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmServiceImplTest {

	@Mock
	private GermplasmService middlewareGermplasmService;

	@Mock
	private GermplasmSearchService germplasmSearchService;

	@Mock
	private GermplasmDeleteValidator germplasmDeleteValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private PedigreeService pedigreeService;

	@InjectMocks
	private GermplasmServiceImpl germplasmServiceImpl;

	@Mock
	private GermplasmNameTypeService germplasmNameTypeService;

	@Test
	public void shouldFilterGermplasmNameTypes() {

		final Set<String> codes = new HashSet() {{
			this.add("LNAME");
		}};

		final GermplasmNameTypeDTO nameTypeDTO = new GermplasmNameTypeDTO();
		nameTypeDTO.setCode("LNAME");
		nameTypeDTO.setId(new Random().nextInt());
		nameTypeDTO.setName("LINE NAME");

		final Set<String> types = Collections.singleton(UDTableType.NAMES_NAME.getType());
		when(this.germplasmNameTypeService.filterGermplasmNameTypes(codes)).thenReturn(Arrays.asList(nameTypeDTO));

		final List<GermplasmNameTypeDTO> germplasmListTypes = this.germplasmServiceImpl.filterGermplasmNameTypes(codes);
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final GermplasmNameTypeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), is(nameTypeDTO.getCode()));
		assertThat(actualGermplasmListTypeDTO.getId(), is(nameTypeDTO.getId()));
		assertThat(actualGermplasmListTypeDTO.getName(), is(nameTypeDTO.getName()));

		Mockito.verify(this.germplasmNameTypeService).filterGermplasmNameTypes(codes);
		Mockito.verifyNoMoreInteractions(this.middlewareGermplasmService);
	}

	@Test
	public void testDeleteGermplasm_WithValidGids() {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3);
		when(this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids)).thenReturn(Sets.newHashSet());
		final GermplasmDeleteResponse response = this.germplasmServiceImpl.deleteGermplasm(gids);

		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.anyList());
		Mockito.verify(this.middlewareGermplasmService).deleteGermplasm(gids);
		Assert.assertThat(response.getDeletedGermplasm(), iterableWithSize(3));
		Assert.assertThat(response.getGermplasmWithErrors(), iterableWithSize(0));
	}

	@Test
	public void testDeleteGermplasm_WithInvalidGermplasmForDeletion() {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3);
		when(this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids)).thenReturn(new HashSet<>(gids));
		final GermplasmDeleteResponse response = this.germplasmServiceImpl.deleteGermplasm(gids);

		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.anyList());
		Mockito.verify(this.middlewareGermplasmService, Mockito.times(0)).deleteGermplasm(ArgumentMatchers.anyList());
		Assert.assertThat(response.getDeletedGermplasm(), iterableWithSize(0));
		Assert.assertThat(response.getGermplasmWithErrors(), iterableWithSize(3));
	}

	@Test
	public void testSearchGermplasm_PopulateHasProgenyAttribute() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGids(gids);

		final List<GermplasmSearchResponse> responseList = new ArrayList<>();
		final GermplasmSearchResponse response1 = new GermplasmSearchResponse();
		response1.setGid(1);
		responseList.add(response1);
		when(this.germplasmSearchService.searchGermplasm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(responseList);

		when(this.middlewareGermplasmService.getGidsOfGermplasmWithDescendants(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result1 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		Assert.assertNull(result1.get(0).getHasProgeny());

		germplasmSearchRequest.getAddedColumnsPropertyIds().add(ColumnLabels.HAS_PROGENY.getName());

		when(this.middlewareGermplasmService.getGidsOfGermplasmWithDescendants(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(1)));
		final List<GermplasmSearchResponse> result2 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result2.get(0).getHasProgeny(), is(true));

		when(this.middlewareGermplasmService.getGidsOfGermplasmWithDescendants(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result3 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result3.get(0).getHasProgeny(), is(false));

	}

	@Test
	public void testSearchGermplasm_PopulateUsedInStudyAttribute() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGids(gids);

		final List<GermplasmSearchResponse> responseList = new ArrayList<>();
		final GermplasmSearchResponse response1 = new GermplasmSearchResponse();
		response1.setGid(1);
		responseList.add(response1);
		when(this.germplasmSearchService.searchGermplasm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(responseList);

		when(this.middlewareGermplasmService.getGermplasmUsedInStudies(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result1 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		Assert.assertNull(result1.get(0).getUsedInStudy());

		germplasmSearchRequest.getAddedColumnsPropertyIds().add(ColumnLabels.USED_IN_STUDY.getName());

		when(this.middlewareGermplasmService.getGermplasmUsedInStudies(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(1)));
		final List<GermplasmSearchResponse> result2 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result2.get(0).getUsedInStudy(), is(true));

		when(this.middlewareGermplasmService.getGermplasmUsedInStudies(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result3 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result3.get(0).getUsedInStudy(), is(false));

	}

	@Test
	public void testSearchGermplasm_PopulateUsedInLockedListAttribute() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGids(gids);

		final List<GermplasmSearchResponse> responseList = new ArrayList<>();
		final GermplasmSearchResponse response1 = new GermplasmSearchResponse();
		response1.setGid(1);
		responseList.add(response1);
		when(this.germplasmSearchService.searchGermplasm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(responseList);

		when(this.middlewareGermplasmService.getGermplasmUsedInLockedList(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result1 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		Assert.assertNull(result1.get(0).getUsedInLockedList());

		germplasmSearchRequest.getAddedColumnsPropertyIds().add(ColumnLabels.USED_IN_LOCKED_LIST.getName());

		when(this.middlewareGermplasmService.getGermplasmUsedInLockedList(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(1)));
		final List<GermplasmSearchResponse> result2 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result2.get(0).getUsedInLockedList(), is(true));

		when(this.middlewareGermplasmService.getGermplasmUsedInLockedList(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result3 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result3.get(0).getUsedInLockedList(), is(false));

	}

}
