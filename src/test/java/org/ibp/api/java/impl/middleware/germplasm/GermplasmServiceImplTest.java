
package org.ibp.api.java.impl.middleware.germplasm;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeSummaryDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.MatcherAssert;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmDeleteValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmMergeRequestDtoValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmUpdateDtoValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator;
import org.ibp.api.java.inventory.manager.LotService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
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

	@Mock
	private GermplasmUpdateDtoValidator germplasmUpdateDtoValidator;

	@Mock
	private GermplasmImportRequestDtoValidator germplasmImportRequestDtoValidator;

	@Mock
	private GermplasmMergeRequestDtoValidator germplasmMergeRequestDtoValidator;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private GermplasmListService germplasmListService;

	@Mock
	private LotService lotService;

	@Mock
	private StudyService studyService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Test
	public void testFilterGermplasmNameTypes() {

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
	public void testSearchGermplasm_PopulateUsedInLockedStudyAttribute() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGids(gids);

		final List<GermplasmSearchResponse> responseList = new ArrayList<>();
		final GermplasmSearchResponse response1 = new GermplasmSearchResponse();
		response1.setGid(1);
		responseList.add(response1);
		when(this.germplasmSearchService.searchGermplasm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(responseList);

		when(this.middlewareGermplasmService.getGermplasmUsedInLockedStudies(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result1 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		Assert.assertNull(result1.get(0).getUsedInLockedStudy());

		germplasmSearchRequest.getAddedColumnsPropertyIds().add(ColumnLabels.USED_IN_LOCKED_STUDY.getName());

		when(this.middlewareGermplasmService.getGermplasmUsedInLockedStudies(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(1)));
		final List<GermplasmSearchResponse> result2 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result2.get(0).getUsedInLockedStudy(), is(true));

		when(this.middlewareGermplasmService.getGermplasmUsedInLockedStudies(Mockito.anyList())).thenReturn(
			new HashSet<>());
		final List<GermplasmSearchResponse> result3 = this.germplasmServiceImpl.searchGermplasm(germplasmSearchRequest, null, null);
		assertThat(result3.get(0).getUsedInLockedStudy(), is(false));

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

	@Test
	public void testSearchNameTypes() {
		final String name = StringUtils.randomAlphanumeric(30);
		this.germplasmServiceImpl.searchNameTypes(name);
		Mockito.verify(this.germplasmNameTypeService).searchNameTypes(name);
	}

	@Test
	public void testCountSearchGermplasm() {
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmUUID(StringUtils.randomAlphanumeric(30));
		final String programUUID = StringUtils.randomAlphanumeric(30);
		Mockito.when(this.germplasmSearchService.countSearchGermplasm(germplasmSearchRequest, programUUID)).thenReturn(5L);
		final long count = this.germplasmServiceImpl.countSearchGermplasm(germplasmSearchRequest, programUUID);
		Mockito.verify(this.germplasmSearchService).countSearchGermplasm(germplasmSearchRequest, programUUID);
		assertThat(count, is(5L));
	}

	@Test
	public void testImportGermplasmUpdates() {
		final GermplasmUpdateDTO updateDTO = new GermplasmUpdateDTO();
		updateDTO.setGid(new Random().nextInt());
		updateDTO.setBreedingMethodAbbr(RandomStringUtils.random(20));
		updateDTO.setLocationAbbreviation(RandomStringUtils.random(20));

		final String programUUID = StringUtils.randomAlphanumeric(30);
		this.germplasmServiceImpl.importGermplasmUpdates(programUUID, Collections.singletonList(updateDTO));
		Mockito.verify(this.germplasmUpdateDtoValidator).validate(programUUID, Collections.singletonList(updateDTO));
		Mockito.verify(this.middlewareGermplasmService).importGermplasmUpdates(programUUID, Collections.singletonList(updateDTO));
	}

	@Test
	public void testImportGermplasm() {
		final GermplasmImportDTO importDTO = new GermplasmImportDTO();
		importDTO.setClientId(1);
		importDTO.setGermplasmPUI(RandomStringUtils.randomAlphabetic(20));
		importDTO.setLocationAbbr(RandomStringUtils.randomAlphabetic(20));
		importDTO.setBreedingMethodAbbr(RandomStringUtils.randomAlphabetic(20));
		final GermplasmImportRequestDto importRequestDto = new GermplasmImportRequestDto();
		importRequestDto.setSkipIfExists(new Random().nextBoolean());
		importRequestDto.setGermplasmList(Collections.singletonList(importDTO));
		importRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);

		final String programUUID = StringUtils.randomAlphanumeric(30);
		final String crop = StringUtils.randomAlphanumeric(30);
		this.germplasmServiceImpl.importGermplasm(crop, programUUID, importRequestDto);
		Mockito.verify(this.germplasmImportRequestDtoValidator).validateBeforeSaving(programUUID, importRequestDto);
		Mockito.verify(this.middlewareGermplasmService).importGermplasm(crop, programUUID, importRequestDto);
	}

	@Test
	public void testCountGermplasmMatches_NullRequest() {
		try {
			this.germplasmServiceImpl.countGermplasmMatches(null);
			Assert.fail("Should have thrown exception but didn't");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.match.request.null"));
			Mockito.verifyZeroInteractions(this.middlewareGermplasmService);
		}
	}

	@Test
	public void testCountGermplasmMatches_InvalidRequest() {
		try {
			this.germplasmServiceImpl.countGermplasmMatches(new GermplasmMatchRequestDto());
			Assert.fail("Should have thrown exception but didn't");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.match.request.invalid"));
			Mockito.verifyZeroInteractions(this.middlewareGermplasmService);
		}
	}

	@Test
	public void testCountGermplasmMatches_MatchByPUI() {
		final GermplasmMatchRequestDto requestDto = new GermplasmMatchRequestDto();
		requestDto.setGermplasmPUIs(Arrays.asList(RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20)));
		final long middlewareCount = new Random().nextLong();
		Mockito.when(this.middlewareGermplasmService.countGermplasmMatches(requestDto)).thenReturn(middlewareCount);
		final long count = this.germplasmServiceImpl.countGermplasmMatches(requestDto);
		assertThat(count, is(middlewareCount));
	}

	@Test
	public void testCountGermplasmMatches_MatchByNames() {
		final GermplasmMatchRequestDto requestDto = new GermplasmMatchRequestDto();
		requestDto.setNames(Arrays.asList(RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20)));
		final long middlewareCount = new Random().nextLong();
		Mockito.when(this.middlewareGermplasmService.countGermplasmMatches(requestDto)).thenReturn(middlewareCount);
		final long count = this.germplasmServiceImpl.countGermplasmMatches(requestDto);
		assertThat(count, is(middlewareCount));
	}

	@Test
	public void testFindGermplasmMatches_NullRequest() {
		try {
			this.germplasmServiceImpl.findGermplasmMatches(null, null);
			Assert.fail("Should have thrown exception but didn't");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.match.request.null"));
			Mockito.verifyZeroInteractions(this.middlewareGermplasmService);
		}
	}

	@Test
	public void testFindGermplasmMatches_InvalidRequest() {
		try {
			this.germplasmServiceImpl.findGermplasmMatches(new GermplasmMatchRequestDto(), null);
			Assert.fail("Should have thrown exception but didn't");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.match.request.invalid"));
			Mockito.verifyZeroInteractions(this.middlewareGermplasmService);
		}
	}

	@Test
	public void testFindGermplasmMatches_MatchByPUI() {
		final GermplasmMatchRequestDto requestDto = new GermplasmMatchRequestDto();
		requestDto.setGermplasmPUIs(Arrays.asList(RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20)));
		this.germplasmServiceImpl.findGermplasmMatches(requestDto, null);
		Mockito.verify(this.middlewareGermplasmService).findGermplasmMatches(requestDto, null);
	}

	@Test
	public void testFindGermplasmMatches_MatchByNames() {
		final GermplasmMatchRequestDto requestDto = new GermplasmMatchRequestDto();
		requestDto.setNames(Arrays.asList(RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20), RandomStringUtils.randomAlphanumeric(20)));
		this.germplasmServiceImpl.findGermplasmMatches(requestDto, null);
		Mockito.verify(this.middlewareGermplasmService).findGermplasmMatches(requestDto, null);
	}

	@Test
	public void testGetGermplasmDtoById_NotExistingGID() {
		final Integer gid = new Random().nextInt();
		Mockito.when(this.middlewareGermplasmService.getGermplasmDtoById(gid)).thenReturn(null);
		try {
			this.germplasmServiceImpl.getGermplasmDtoById(gid);
			Assert.fail("Should have thrown exception but didn't");
		} catch (final ResourceNotFoundException e) {
			assertThat(Arrays.asList(e.getError().getCodes()), hasItem("gids.invalid"));
		}
	}

	@Test
	public void testGetGermplasmDtoById() {
		final Integer gid = new Random().nextInt();
		final GermplasmDto germplasmDto = new GermplasmDto();
		germplasmDto.setGermplasmUUID(RandomStringUtils.randomAlphabetic(20));
		Mockito.when(this.middlewareGermplasmService.getGermplasmDtoById(gid)).thenReturn(germplasmDto);
		final GermplasmDto germplasmDtoById = this.germplasmServiceImpl.getGermplasmDtoById(gid);
		assertThat(germplasmDtoById, is(germplasmDto));
	}

	@Test
	public void testSearchGermplasmCount() {
		final String text = StringUtils.randomAlphanumeric(30);
		final GermplasmSearchParameter searchParameter = new GermplasmSearchParameter(text, Operation.LIKE, false, false, false);
		Mockito.when(this.germplasmDataManager.countSearchForGermplasm(searchParameter)).thenReturn(5);
		final long count = this.germplasmServiceImpl.searchGermplasmCount(text);
		Mockito.verify(this.germplasmDataManager).countSearchForGermplasm(searchParameter);
		assertThat(count, is(5L));
	}

	@Test
	public void testMergeGermplasm() {
		final GermplasmMergeRequestDto requestDto = new GermplasmMergeRequestDto();
		requestDto.setTargetGermplasmId(new Random().nextInt());
		this.germplasmServiceImpl.mergeGermplasm(requestDto);
		Mockito.verify(this.germplasmMergeRequestDtoValidator).validate(requestDto);
		Mockito.verify(this.middlewareGermplasmService).mergeGermplasm(requestDto, this.pedigreeService.getCrossExpansion(requestDto.getTargetGermplasmId(), this.crossExpansionProperties));
	}

	@Test
	public void testGetGermplasmMerged() {
		final Integer gid = new Random().nextInt();
		this.germplasmServiceImpl.getGermplasmMerged(gid);
		Mockito.verify(this.germplasmValidator).validateGermplasmId(ArgumentMatchers.any(), ArgumentMatchers.eq(gid));
		Mockito.verify(this.middlewareGermplasmService).getGermplasmMerged(gid);
	}

	@Test
	public void testGetGermplasmProgenies() {
		final Integer gid = new Random().nextInt();
		this.germplasmServiceImpl.getGermplasmProgenies(gid);
		Mockito.verify(this.germplasmValidator).validateGermplasmId(ArgumentMatchers.any(), ArgumentMatchers.eq(gid));
		Mockito.verify(this.middlewareGermplasmService).getGermplasmProgenies(gid);
	}

	@Test
	public void testgetGermplasmProgenitorDetails() {
		final Integer gid = new Random().nextInt();
		this.germplasmServiceImpl.getGermplasmProgenitorDetails(gid);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.middlewareGermplasmService).getGermplasmProgenitorDetails(gid);
	}

	@Test
	public void testGetGermplasmMergeSummary() {
		final GermplasmMergeRequestDto requestDto = new GermplasmMergeRequestDto();
		requestDto.setTargetGermplasmId(new Random().nextInt());
		requestDto.setNonSelectedGermplasm(
			Lists.newArrayList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, true, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false),
		new GermplasmMergeRequestDto.NonSelectedGermplasm(4, false, true)));
		final long listsToUpdate = new Random().nextInt(100);
		final long studiesToUpdate = new Random().nextInt(100);
		final long plotsToUpdate = new Random().nextInt(1000);
		when(this.germplasmListService.countGermplasmLists(Mockito.anyList())).thenReturn(listsToUpdate);
		when(this.studyService.countStudiesByGids(Mockito.anyList())).thenReturn(studiesToUpdate);
		when(this.studyService.countPlotsByGids(Mockito.anyList())).thenReturn(plotsToUpdate);

		final LotsSearchDto migrateLotsDto = new LotsSearchDto();
		migrateLotsDto.setGids(Collections.singletonList(2));
		final long lotsToMigrate = new Random().nextInt(100);
		when(this.lotService.countSearchLots(migrateLotsDto)).thenReturn(lotsToMigrate);

		final long lotsToClose = new Random().nextInt(100);
		final LotsSearchDto closeLotsDto = new LotsSearchDto();
		closeLotsDto.setGids(Collections.singletonList(3));
		when(this.lotService.countSearchLots(closeLotsDto)).thenReturn(lotsToClose);

		final GermplasmMergeSummaryDto summary = this.germplasmServiceImpl.getGermplasmMergeSummary(requestDto);
		Mockito.verify(this.germplasmMergeRequestDtoValidator).validate(requestDto);
		// Verify that non-selected germplasm for omission was excluded
		assertThat(summary.getCountGermplasmToDelete(), is(2L));
		assertThat(summary.getCountListsToUpdate(), is(listsToUpdate));
		assertThat(summary.getCountStudiesToUpdate(), is(studiesToUpdate));
		assertThat(summary.getCountLotsToMigrate(), is(lotsToMigrate));
		assertThat(summary.getCountLotsToClose(), is(lotsToClose));
	}


}
