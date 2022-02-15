package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.generationcp.middleware.dao.germplasmlist.GermplasmListDataDAO;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListDataValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

public class GermplasmListServiceImplTest {

	private static final int GERMPLASM_LIST_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final String GERMPLASM_LIST_NAME = UUID.randomUUID().toString();
	private static final Date GERMPLASM_LIST_DATE = new Date();
	private static final String GERMPLASM_LIST_DESCRIPTION = UUID.randomUUID().toString();
	private static final String GERMPLASM_LIST_TYPE = "LST";
	private static final int GID1 = 1;
	private static final int GID2 = 2;
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();
	private static final Integer USER_ID = new Random().nextInt();
	private static final String CROP = "maize";
	private WorkbenchUser loggedInUser;

	@InjectMocks
	private GermplasmListServiceImpl germplasmListService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ProgramValidator programValidator;

	@Mock
	public SecurityService securityService;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private GermplasmListService germplasmListServiceMiddleware;

	@Mock
	private GermplasmListDataService germplasmListDataService;

	@Mock
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private GermplasmListValidator germplasmListValidator;

	@Mock
	private GermplasmListDataValidator germplasmListDataValidator;

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);
		ContextHolder.setCurrentProgram(PROGRAM_UUID);

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode(GERMPLASM_LIST_TYPE);
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("GERMPLASM LISTS");

		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Collections.singletonList(userDefinedField));
		final List<Germplasm> germplasms = new ArrayList<>();
		germplasms.add(new Germplasm(GID1));
		germplasms.add(new Germplasm(GID2));
		Mockito.when(this.germplasmDataManager.getGermplasms(ArgumentMatchers.anyList())).thenReturn(germplasms);

		final HashMap<Integer, String> plotCodeValuesByGids = new HashMap<>();
		plotCodeValuesByGids.put(GID1, GermplasmListDataDAO.SOURCE_UNKNOWN);
		plotCodeValuesByGids.put(GID2, GermplasmListDataDAO.SOURCE_UNKNOWN);
		Mockito.when(this.germplasmService.getPlotCodeValues(ArgumentMatchers.anySet())).thenReturn(plotCodeValuesByGids);

		if (this.loggedInUser == null) {
			this.loggedInUser = new WorkbenchUser(USER_ID);
		}
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.loggedInUser);
	}

	@Test
	public void shouldGetGermplasmListTypes() {

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode("CHECK");
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("CHECK LIST");

		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Collections.singletonList(userDefinedField));

		final List<GermplasmListTypeDTO> germplasmListTypes = this.germplasmListService.getGermplasmListTypes();
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final GermplasmListTypeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), Matchers.is(userDefinedField.getFcode()));
		assertThat(actualGermplasmListTypeDTO.getId(), Matchers.is(userDefinedField.getFldno()));
		assertThat(actualGermplasmListTypeDTO.getName(), Matchers.is(userDefinedField.getFname()));

		Mockito.verify(this.germplasmListManager).getGermplasmListTypes();
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
	}

	@Test
	public void testCreate_ShouldValidateGid() {
		try {
			final GermplasmListGeneratorDTO request = this.createGermplasmList();
			request.getEntries().get(1).setGid(null);
			this.germplasmListService.create(request);
			Assert.fail();
		} catch (final ApiValidationException e) {
			Assert.assertThat(e.getErrorCode(), is("error.germplasmlist.save.gid"));
		}
	}

	@Test
	public void testCreate_ShouldValidateEntryNo() {
		try {
			final GermplasmListGeneratorDTO request = this.createGermplasmList();
			request.getEntries().get(1).setEntryNo(40);
			this.germplasmListService.create(request);
			Assert.fail();
		} catch (final ApiValidationException e) {
			Assert.assertThat(e.getErrorCode(), is("error.germplasmlist.save.entryno.gaps"));
		}
	}

	@Test
	public void testCreate_ShouldHaveAutoGeneratedEntryNo() {
		final GermplasmListGeneratorDTO request = this.createGermplasmList();
		this.germplasmListService.create(request);
		Assert.assertThat(request.getEntries().get(1).getEntryNo(), is(2));
	}

	@Test
	public void testCreate_OK() {
		final GermplasmListGeneratorDTO request = this.createGermplasmList();
		request.setProgramUUID(PROGRAM_UUID);
		request.setStatus(GermplasmList.Status.LIST.getCode());

		this.germplasmListService.create(request);
		final ArgumentCaptor<GermplasmListGeneratorDTO> metadataRequestCaptor = ArgumentCaptor.forClass(GermplasmListGeneratorDTO.class);
		Mockito.verify(this.germplasmListValidator)
			.validateListMetadata(metadataRequestCaptor.capture(), ArgumentMatchers.eq(PROGRAM_UUID));
		final GermplasmListGeneratorDTO metadata = metadataRequestCaptor.getValue();
		Assert.assertEquals(request.getCreationDate(), metadata.getCreationDate());
		Assert.assertEquals(request.getListName(), metadata.getListName());
		Assert.assertEquals(request.getDescription(), metadata.getDescription());
		Assert.assertEquals(request.getNotes(), metadata.getNotes());
		Assert.assertEquals(request.getListType(), metadata.getListType());
		Mockito.verify(this.germplasmListServiceMiddleware)
			.create(request, this.loggedInUser.getUserid());
	}

	@Test
	public void testAddGermplasmEntriesToList_WithSelectedItems_OK() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);
		Mockito.when(searchComposite.getItemIds()).thenReturn(new HashSet<>(Collections.singletonList(new Random().nextInt())));

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));

		final GermplasmList germplasmList = new GermplasmList(germplasmListId);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(germplasmListId)).thenReturn(germplasmList);

		Mockito.doNothing().when(this.germplasmValidator)
			.validateGids(any(MapBindingResult.class), ArgumentMatchers.anyList());

		this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.germplasmListValidator).validateListIsNotAFolder(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);

		Mockito.verify(this.germplasmValidator).validateGids(any(MapBindingResult.class), ArgumentMatchers.anyList());

		Mockito.verify(this.germplasmListServiceMiddleware).addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testAddGermplasmEntriesToList_WithoutSelectedItems_OK() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));

		final GermplasmList germplasmList = new GermplasmList(germplasmListId);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(germplasmListId)).thenReturn(germplasmList);

		this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.germplasmListValidator).validateListIsNotAFolder(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);

		Mockito.verifyNoInteractions(this.germplasmValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testAddGermplasmEntriesToList_GermplasmListIsFolder_FAIL() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));

		final GermplasmList germplasmList = new GermplasmList(germplasmListId);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(germplasmListId)).thenReturn(germplasmList);
		final String errorCode = RandomStringUtils.randomAlphabetic(10);
		Mockito.doThrow(new ApiRequestValidationException(errorCode, null))
			.when(this.germplasmListValidator).validateListIsNotAFolder(germplasmList);
		try {
			this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);
			Assert.fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals(errorCode, e.getErrors().get(0).getCode());
		}

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));
		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.germplasmListValidator).validateListIsNotAFolder(germplasmList);
		Mockito.verify(this.germplasmListValidator, Mockito.never()).validateListIsUnlocked(germplasmList);

		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmValidator);
	}

	@Test
	public void testAddGermplasmEntriesToList_GermplasmListIsLocked_FAIL() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));

		final GermplasmList germplasmList = new GermplasmList(germplasmListId);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(germplasmListId)).thenReturn(germplasmList);
		final String errorCode = RandomStringUtils.randomAlphabetic(10);
		Mockito.doThrow(new ApiRequestValidationException(errorCode, null))
			.when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);

		try {
			this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);
			Assert.fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals(errorCode, e.getErrors().get(0).getCode());
		}

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			eq(searchComposite),
			any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.germplasmListValidator).validateListIsNotAFolder(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmValidator);
	}

	@Test
	public void testGetGermplasmLists_WithNoErrors() {
		final Integer gid = 1;
		final GermplasmListDto dto = new GermplasmListDto();
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmLists(gid)).thenReturn(Collections.singletonList(dto));

		final List<GermplasmListDto> germplasmListDtos = this.germplasmListService.getGermplasmLists(gid);

		Assert.assertEquals(dto, germplasmListDtos.get(0));
		Mockito.verify(this.germplasmValidator).validateGids(
			any(BindingResult.class),
			eq(Collections.singletonList(gid)));
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmLists(gid);
	}

	@Test
	public void testGetGermplasmLists_ThrowsException_WhenGIDIsInvalid() {
		final Integer gid = 999;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(any(BindingResult.class), eq(Collections.singletonList(gid)));
			this.germplasmListService.getGermplasmLists(gid);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(
				any(BindingResult.class),
				eq(Collections.singletonList(gid)));
			Mockito.verify(this.germplasmListServiceMiddleware, Mockito.never()).getGermplasmLists(gid);
		}
	}

	@Test
	public void getGermplasmListById() {
		final GermplasmList germplasmList = this.createGermplasmListMock(true);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);

		final GermplasmListDto dto = this.germplasmListService.getGermplasmListById(GERMPLASM_LIST_ID);
		assertNotNull(dto);
		assertThat(dto.getListId(), is(GERMPLASM_LIST_ID));
		assertThat(dto.getListName(), is(GERMPLASM_LIST_NAME));
		assertThat(dto.getCreationDate(), is(GERMPLASM_LIST_DATE));
		assertThat(dto.getDescription(), is(GERMPLASM_LIST_DESCRIPTION));
		assertThat(dto.getProgramUUID(), is(PROGRAM_UUID));
		assertTrue(dto.isLocked());

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
	}

	@Test
	public void toggleGermplasmListStatus_OK() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.when(this.germplasmListServiceMiddleware.toggleGermplasmListStatus(GERMPLASM_LIST_ID)).thenReturn(true);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

		final boolean status = this.germplasmListService.toggleGermplasmListStatus(GERMPLASM_LIST_ID);
		assertTrue(status);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListServiceMiddleware).toggleGermplasmListStatus(GERMPLASM_LIST_ID);
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
	}

	@Test
	public void toggleGermplasmListStatus_notOwner() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(new Random().nextInt()));

		try {
			this.germplasmListService.toggleGermplasmListStatus(GERMPLASM_LIST_ID);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(e.getErrors().get(0).getCode(), is("list.toggle.status.not.owner"));
		}

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
	}

	@Test
	public void toggleGermplasmListStatus_notOwner_userHasAdminPermission() {

		final Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));

		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.when(this.germplasmListServiceMiddleware.toggleGermplasmListStatus(GERMPLASM_LIST_ID)).thenReturn(true);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(new Random().nextInt()));

		try (final MockedStatic<SecurityUtil> utilMockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
			utilMockedStatic.when(SecurityUtil::getLoggedInUserAuthorities).thenReturn(authorities);
			final boolean status = this.germplasmListService.toggleGermplasmListStatus(GERMPLASM_LIST_ID);
			assertTrue(status);

			Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
			Mockito.verify(this.germplasmListServiceMiddleware).toggleGermplasmListStatus(GERMPLASM_LIST_ID);
			Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
		}

	}

	@Test
	public void testGetGermplasmListVariables_OK() {
		this.germplasmListService.getGermplasmListVariables(PROGRAM_UUID, GERMPLASM_LIST_ID, VariableType.ENTRY_DETAIL.getId());
		Mockito.verify(this.germplasmListServiceMiddleware)
			.getGermplasmListVariables(PROGRAM_UUID, GERMPLASM_LIST_ID, VariableType.ENTRY_DETAIL.getId());
	}

	@Test
	public void testImportUpdates_OK() {

		final GermplasmListGeneratorDTO germplasmListGeneratorDTO = Mockito.mock(GermplasmListGeneratorDTO.class);
		Mockito.when(germplasmListGeneratorDTO.getListId()).thenReturn(GERMPLASM_LIST_ID);

		this.createMockListEntries(germplasmListGeneratorDTO, 2L);

		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);

		Mockito.doNothing().when(this.germplasmListServiceMiddleware).importUpdates(germplasmListGeneratorDTO);

		this.germplasmListService.importUpdates(germplasmListGeneratorDTO);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).importUpdates(germplasmListGeneratorDTO);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testImportUpdates_listIsLocked() {
		final GermplasmListGeneratorDTO germplasmListGeneratorDTO = Mockito.mock(GermplasmListGeneratorDTO.class);
		Mockito.when(germplasmListGeneratorDTO.getListId()).thenReturn(GERMPLASM_LIST_ID);
		Mockito.when(germplasmListGeneratorDTO.getEntries()).thenReturn(Arrays.asList());

		final GermplasmList germplasmList = this.createGermplasmListMock(true);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doThrow(ApiRequestValidationException.class).when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);

		this.germplasmListService.importUpdates(germplasmListGeneratorDTO);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);

		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testImportUpdates_invalidEntryNo() throws ApiRequestValidationException {
		final GermplasmListGeneratorDTO germplasmListGeneratorDTO = Mockito.mock(GermplasmListGeneratorDTO.class);
		Mockito.when(germplasmListGeneratorDTO.getListId()).thenReturn(GERMPLASM_LIST_ID);

		this.createMockListEntries(germplasmListGeneratorDTO, 1L);

		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);

		Mockito.doNothing().when(this.germplasmListServiceMiddleware).importUpdates(germplasmListGeneratorDTO);

		try {
			this.germplasmListService.importUpdates(germplasmListGeneratorDTO);
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert
				.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
					hasItem("invalid.entry.no.value"));
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testEditListMetadata_NoListId_Fail() {
		final GermplasmListDto request = new GermplasmListDto();
		request.setListName(RandomStringUtils.randomAlphabetic(20));
		request.setDescription(RandomStringUtils.randomAlphabetic(20));
		request.setNotes(RandomStringUtils.randomAlphabetic(20));
		request.setCreationDate(new Date());
		request.setListType(GERMPLASM_LIST_TYPE);
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verifyNoInteractions(this.germplasmListValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testEditListMetadata_TryingToUpdateListOwner_Fail() {
		final GermplasmList germplasmList = new GermplasmList(GERMPLASM_LIST_ID);
		germplasmList.setUserId(1);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmListDto request = new GermplasmListDto();
		request.setListId(GERMPLASM_LIST_ID);
		request.setOwnerId(2);
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testEditListMetadata_TryingToLockList_Fail() {
		final GermplasmList germplasmList = new GermplasmList(GERMPLASM_LIST_ID);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmListDto request = new GermplasmListDto();
		request.setListId(GERMPLASM_LIST_ID);
		request.setLocked(true);
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testEditListMetadata_TryingToUnlockList_Fail() {
		final GermplasmList germplasmList = new GermplasmList(GERMPLASM_LIST_ID);
		germplasmList.lockList();
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmListDto request = new GermplasmListDto();
		request.setListId(GERMPLASM_LIST_ID);
		request.setLocked(false);
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testEditListMetadata_TryingToUpdateListProgram_Fail() {
		final GermplasmList germplasmList = new GermplasmList(GERMPLASM_LIST_ID);
		germplasmList.setProgramUUID(RandomStringUtils.randomAlphabetic(30));
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmListDto request = new GermplasmListDto();
		request.setListId(GERMPLASM_LIST_ID);
		request.setProgramUUID(PROGRAM_UUID);
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testEditListMetadata_TryingToUpdateParentFolder_Fail() {
		final GermplasmList germplasmList = new GermplasmList(GERMPLASM_LIST_ID);
		germplasmList.setParent(new GermplasmList(1));
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmListDto request = new GermplasmListDto();
		request.setListId(GERMPLASM_LIST_ID);
		request.setParentFolderId("2");
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testEditListMetadata_TryingToUpdateStatus_Fail() {
		final GermplasmList germplasmList = new GermplasmList(GERMPLASM_LIST_ID);
		germplasmList.setStatus(0);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmListDto request = new GermplasmListDto();
		request.setListId(GERMPLASM_LIST_ID);
		request.setStatus(9);
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testEditListMetadata_OK() {
		final GermplasmList germplasmList = new GermplasmList(GERMPLASM_LIST_ID);
		germplasmList.setProgramUUID(PROGRAM_UUID);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmListDto request = new GermplasmListDto();
		request.setListId(GERMPLASM_LIST_ID);
		request.setListName(RandomStringUtils.randomAlphabetic(20));
		request.setDescription(RandomStringUtils.randomAlphabetic(20));
		request.setNotes(RandomStringUtils.randomAlphabetic(20));
		request.setCreationDate(new Date());
		request.setListType(GERMPLASM_LIST_TYPE);
		this.germplasmListService.editListMetadata(request, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsNotAFolder(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateListMetadata(request, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).editListMetadata(request);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testDeleteGermplasmList_listIsLocked() {
		final GermplasmList germplasmList = this.createGermplasmListMock(true);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doThrow(ApiRequestValidationException.class).when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		this.germplasmListService.deleteGermplasmList(CROP, PROGRAM_UUID, GERMPLASM_LIST_ID);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testDeleteGermplasmList_OK() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		this.germplasmListService.deleteGermplasmList(CROP, PROGRAM_UUID, GERMPLASM_LIST_ID);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verify(this.germplasmListServiceMiddleware).deleteGermplasmList(GERMPLASM_LIST_ID);
	}

	@Test
	public void testAddGermplasmListEntriesToAnotherList_OK() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		final GermplasmList sourceGermplasmList = this.createGermplasmListMock(false);
		final int sourceGermplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(sourceGermplasmListId)).thenReturn(sourceGermplasmList);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			any(SearchCompositeDto.class),
			any(MapBindingResult.class));

		this.germplasmListService.addGermplasmListEntriesToAnotherList(CROP, PROGRAM_UUID, GERMPLASM_LIST_ID, sourceGermplasmListId,
			new SearchCompositeDto<>());

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateGermplasmList(sourceGermplasmListId);
		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(
			any(SearchCompositeDto.class),
			any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verify(this.germplasmListServiceMiddleware)
			.addGermplasmListEntriesToAnotherList(GERMPLASM_LIST_ID, sourceGermplasmListId, PROGRAM_UUID, new SearchCompositeDto<>());
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testAddGermplasmListEntriesToAnotherList_listIsLocked() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doThrow(ApiRequestValidationException.class).when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		final int sourceGermplasmListId = new Random().nextInt(Integer.MAX_VALUE);

		this.germplasmListService.addGermplasmListEntriesToAnotherList(CROP, PROGRAM_UUID, GERMPLASM_LIST_ID, sourceGermplasmListId,
			new SearchCompositeDto<>());

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testCloneGermplasmList_OK() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);

		final GermplasmListDto request = this.createGermplasmListRequest();
		final String parentFolderId = request.getParentFolderId();
		this.germplasmListService.clone(GERMPLASM_LIST_ID, request);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListMetadata(request, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateParentFolder(parentFolderId);
		Mockito.verify(this.germplasmListValidator).validateFolderId(parentFolderId, PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).cloneGermplasmList(anyInt(), any(GermplasmListDto.class), anyInt());
	}

	@Test
	public void testRemoveEntriesFromList_OK() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);

		final List<Integer> listDataIds = Arrays.asList(1);
		final SearchCompositeDto<GermplasmListDataSearchRequest, Integer> searchComposite = new SearchCompositeDto<>();
		searchComposite.setItemIds(new HashSet<>(listDataIds));
		this.germplasmListService.removeGermplasmEntriesFromList(GERMPLASM_LIST_ID, searchComposite);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateListIsNotAFolder(germplasmList);
		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(eq(searchComposite), any(BindingResult.class));
		Mockito.verify(this.germplasmListDataValidator).verifyListDataIdsExist(GERMPLASM_LIST_ID, listDataIds);
		Mockito.verify(this.germplasmListServiceMiddleware).removeGermplasmEntriesFromList(GERMPLASM_LIST_ID, searchComposite);
	}

	private GermplasmListGeneratorDTO createGermplasmList() {
		final GermplasmListGeneratorDTO list = new GermplasmListGeneratorDTO();
		list.setListName(RandomStringUtils.random(50));
		list.setDescription(RandomStringUtils.random(255));
		list.setCreationDate(new Date());
		list.setListType(GERMPLASM_LIST_TYPE);
		list.setParentFolderId(GermplasmListTreeServiceImpl.PROGRAM_LISTS);
		final List<GermplasmListGeneratorDTO.GermplasmEntryDTO> entries = new ArrayList<>();
		final GermplasmListGeneratorDTO.GermplasmEntryDTO entry1 = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
		entry1.setGid(GID1);
		entry1.setSeedSource(RandomStringUtils.random(255));
		entries.add(entry1);
		final GermplasmListGeneratorDTO.GermplasmEntryDTO entry2 = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
		entry2.setGid(GID2);
		entry2.setSeedSource(RandomStringUtils.random(255));
		entries.add(entry2);
		list.setEntries(entries);
		return list;
	}

	private GermplasmListDto createGermplasmListRequest() {
		final GermplasmListDto list = new GermplasmListDto();
		list.setListName(RandomStringUtils.random(50));
		list.setDescription(RandomStringUtils.random(255));
		list.setCreationDate(new Date());
		list.setListType(GERMPLASM_LIST_TYPE);
		list.setParentFolderId(GermplasmListTreeServiceImpl.PROGRAM_LISTS);
		return list;
	}

	private GermplasmList createGermplasmListMock(final boolean isLocked) {
		final GermplasmList mock = Mockito.mock(GermplasmList.class);
		Mockito.when(mock.getId()).thenReturn(GERMPLASM_LIST_ID);
		Mockito.when(mock.getName()).thenReturn(GERMPLASM_LIST_NAME);
		Mockito.when(mock.parseDate()).thenReturn(GERMPLASM_LIST_DATE);
		Mockito.when(mock.getDescription()).thenReturn(GERMPLASM_LIST_DESCRIPTION);
		Mockito.when(mock.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(mock.getUserId()).thenReturn(USER_ID);
		Mockito.when(mock.isLockedList()).thenReturn(isLocked);
		return mock;
	}

	private void createMockListEntries(final GermplasmListGeneratorDTO germplasmListGeneratorDTO, final long expectedEntriesCount) {
		final GermplasmListGeneratorDTO.GermplasmEntryDTO entry1 = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
		entry1.setEntryNo(1);
		final GermplasmListGeneratorDTO.GermplasmEntryDTO entry2 = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
		entry2.setEntryNo(2);
		Mockito.when(germplasmListGeneratorDTO.getEntries()).thenReturn(Arrays.asList(entry1, entry2));

		Mockito.when(this.germplasmListDataService.countSearchGermplasmListData(germplasmListGeneratorDTO.getListId(),
			new GermplasmListDataSearchRequest())).thenReturn(expectedEntriesCount);
	}

}
