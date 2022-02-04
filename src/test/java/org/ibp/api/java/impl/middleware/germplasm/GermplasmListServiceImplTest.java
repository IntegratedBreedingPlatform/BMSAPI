package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.dao.germplasmlist.GermplasmListDataDAO;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListDataValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.rest.common.UserTreeState;
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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

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
	private CrossExpansionProperties crossExpansionProperties;

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
	private UserValidator userValidator;

	@Mock
	private UserProgramStateDataManager userProgramStateDataManager;

	@Mock
	private GermplasmListValidator germplasmListValidator;

	@Mock
	private GermplasmListDataValidator germplasmListDataValidator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
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

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_MissingFolderOnly_ThrowsException() throws ApiRequestValidationException {
		this.germplasmListService.getGermplasmListChildrenNodes(CROP, null, null, null);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_InvalidParentId_ThrowsException() throws ApiRequestValidationException {
		this.germplasmListService.getGermplasmListChildrenNodes(CROP, null, "X", Boolean.TRUE);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_InvalidFolderId_ThrowsException() throws ApiRequestValidationException {
		final String parentId = "1";
		final GermplasmList germplasmList = new GermplasmList();
		final String program = RandomStringUtils.randomAlphabetic(3);
		Mockito.when(this.germplasmListManager.getGermplasmListById(Integer.parseInt(parentId))).thenReturn(germplasmList);
		this.germplasmListService.getGermplasmListChildrenNodes(CROP, program, parentId, Boolean.FALSE);

	}

	@Test
	public void testGetGermplasmListChildrenNodes_NoProgramSpecified_ReturnOnlyCropFolder() throws ApiRequestValidationException {
		final List<TreeNode> result = this.germplasmListService.getGermplasmListChildrenNodes(CROP, null, null, Boolean.FALSE);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(GermplasmListServiceImpl.CROP_LISTS, result.get(0).getKey());
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ProgramIsSpecified_ReturnCropAndProgramFolder() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);

		final List<TreeNode> result = this.germplasmListService.getGermplasmListChildrenNodes(CROP, program, null, Boolean.FALSE);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(GermplasmListServiceImpl.CROP_LISTS, result.get(0).getKey());
		Assert.assertEquals(GermplasmListServiceImpl.PROGRAM_LISTS, result.get(1).getKey());

	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsCropList_LoadCropGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		this.germplasmListService.getGermplasmListChildrenNodes(CROP, program, GermplasmListServiceImpl.CROP_LISTS, Boolean.FALSE);
		Mockito.verify(this.germplasmListManager, times(1)).getAllTopLevelLists(null);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsProgramList_LoadProgramGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		this.germplasmListService.getGermplasmListChildrenNodes(CROP, program, GermplasmListServiceImpl.PROGRAM_LISTS, Boolean.FALSE);
		Mockito.verify(this.germplasmListManager, times(1)).getAllTopLevelLists(program);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsAFolder_LoadLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		final String parentId = "1";

		final GermplasmList germplasmList = this.getGermplasmList(new Random().nextInt());
		germplasmList.setProgramUUID(program);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(Integer.parseInt(parentId)))
			.thenReturn(Optional.of(germplasmList));

		this.germplasmListService.getGermplasmListChildrenNodes(CROP, program, parentId, Boolean.FALSE);
		Mockito.verify(this.germplasmListManager, times(1))
			.getGermplasmListByParentFolderIdBatched(Integer.parseInt(parentId), program, GermplasmListServiceImpl.BATCH_SIZE);
	}

	private GermplasmList getGermplasmList(final Integer id) {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(GermplasmList.FOLDER_TYPE);
		germplasmList.setProgramUUID(GermplasmListServiceImplTest.PROGRAM_UUID);
		germplasmList.setId(id);
		return germplasmList;
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
	public void testCreate_ShouldValidateEntryCode() {
		try {
			final GermplasmListGeneratorDTO request = this.createGermplasmList();
			request.getEntries().get(1).setEntryCode("Entry2");
			this.germplasmListService.create(request);
			Assert.fail();
		} catch (final ApiValidationException e) {
			Assert.assertThat(e.getErrorCode(), is("error.germplasmlist.save.gaps"));
			Assert.assertThat(e.getParams()[0], is(GermplasmListServiceImpl.ENTRY_CODE));
		}
	}

	@Test
	public void testCreate_ShouldHaveAutoGeneratedEntryCode() {
		final GermplasmListGeneratorDTO request = this.createGermplasmList();
		this.germplasmListService.create(request);
		Assert.assertThat(request.getEntries().get(1).getEntryCode(), is(String.valueOf(request.getEntries().get(1).getEntryNo())));
	}

	@Test
	public void testCreate_OK() {
		final GermplasmListGeneratorDTO request = this.createGermplasmList();
		request.setProgramUUID(PROGRAM_UUID);
		request.setStatus(GermplasmList.Status.LIST.getCode());

		this.germplasmListService.create(request);
		final ArgumentCaptor<GermplasmListDto> metadataRequestCaptor = ArgumentCaptor.forClass(GermplasmListDto.class);
		Mockito.verify(this.germplasmListValidator)
			.validateListMetadata(metadataRequestCaptor.capture(), ArgumentMatchers.eq(PROGRAM_UUID));
		final GermplasmListDto metadata = metadataRequestCaptor.getValue();
		Assert.assertEquals(request.getDate(), metadata.getCreationDate());
		Assert.assertEquals(request.getName(), metadata.getListName());
		Assert.assertEquals(request.getDescription(), metadata.getDescription());
		Assert.assertEquals(request.getNotes(), metadata.getNotes());
		Assert.assertEquals(request.getType(), metadata.getListType());
		Mockito.verify(this.germplasmListServiceMiddleware)
			.create(request, this.loggedInUser.getUserid());
	}

	@Test
	public void shouldCreateGermplasmListFolder() {

		final String folderName = "newFolderName";
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newFolderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(parentId))
			.thenReturn(Optional.of(germplasmList));

		Mockito.doNothing().when(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.doNothing().when(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.createGermplasmListFolder(USER_ID, folderName, parentId, PROGRAM_UUID))
			.thenReturn(newFolderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, String.valueOf(parentId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(newFolderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(parentId);
		Mockito.verify(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.verify(this.germplasmListServiceMiddleware).createGermplasmListFolder(USER_ID, folderName, parentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldCreateGermplasmListFolderWithProgramListAsParent() {

		final String folderName = StringUtils.repeat("a", GermplasmListValidator.NAME_MAX_LENGTH);
		final String parentId = GermplasmListServiceImpl.PROGRAM_LISTS;
		final Integer newFolderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());

		Mockito.doNothing().when(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.doNothing().when(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, null, PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.createGermplasmListFolder(USER_ID, folderName, null, PROGRAM_UUID))
			.thenReturn(newFolderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(newFolderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.verify(this.germplasmListServiceMiddleware).createGermplasmListFolder(USER_ID, folderName, null, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailCreateGermplasmListFolderIfParentNotExists() {

		final String folderName = "newFolderName";
		final String parentId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		Mockito.doNothing().when(this.programValidator).validate(any(), any());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(Integer.parseInt(parentId)))
			.thenReturn(Optional.empty());

		try {
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(Integer.parseInt(parentId));
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldUpdateGermplasmListFolder() {

		final String folderName = StringUtils.repeat("a", GermplasmListValidator.NAME_MAX_LENGTH);
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getParentId()).thenReturn(parentId);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(germplasmList.isFolder()).thenReturn(true);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(germplasmList));

		Mockito.doNothing().when(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.doNothing().when(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.updateGermplasmListFolder(folderName, folderId))
			.thenReturn(folderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, String.valueOf(folderId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).updateGermplasmListFolder(folderName, folderId);
		Mockito.verify(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateFolderName(folderName);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmIfFolderIsProgramList() {

		final String folderName = "newFolderName";
		final String folderId = GermplasmListServiceImpl.PROGRAM_LISTS;

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmIfFolderIsCropList() {

		final String folderName = "newFolderName";
		final String folderId = GermplasmListServiceImpl.CROP_LISTS;

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmListFolderIfParentNotExists() {

		final String folderName = "newFolderName";
		final String folderId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		Mockito.doNothing().when(this.programValidator).validate(any(), any());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(Integer.parseInt(folderId), PROGRAM_UUID))
			.thenReturn(Optional.empty());

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(Integer.parseInt(folderId));
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmListFolderIfNotFolder() {

		final String folderName = "newFolderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.isFolder()).thenReturn(false);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(germplasmList));

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, String.valueOf(folderId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldMoveGermplasmListFolder() {
		final String folderName = "folderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getParentId()).thenReturn(newParentId);
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.isFolder()).thenReturn(true);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(newParentId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldMoveGermplasmListFolderToProgramListAsParent() {
		final String folderName = "folderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final String newParentId = GermplasmListServiceImpl.PROGRAM_LISTS;

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getParentId()).thenReturn(null);
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, null, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), newParentId);
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, null, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListWithNullFolderId() {
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, null, String.valueOf(parentId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListWithNullNewParentFolderId() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), null);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.parent.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListWithSameFolderIdAndNewParentId() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(folderId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.move.id.same.values"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldMoveGermplasmListFromProgramListToCropList() {
		final String folderName = "folderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getParentId()).thenReturn(null);
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, null, null))
			.thenReturn(Optional.empty());
		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, null, null))
			.thenReturn(folderId);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(folderId)).thenReturn(actualFolder);

		final Integer actualFolderId = this.germplasmListService
			.moveGermplasmListFolder(CROP, null, String.valueOf(folderId), GermplasmListServiceImpl.CROP_LISTS);
		assertThat(actualFolderId, is(folderId));

		Mockito.verifyNoInteractions(this.programValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, null, null);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, null, null);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);

	}

	@Test
	public void shouldMoveGermplasmListFromCropListToProgramList() {
		final String folderName = "folderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getParentId()).thenReturn(null);
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(null);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, null))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, null, null))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, null, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer actualFolderId = this.germplasmListService
			.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), GermplasmListServiceImpl.PROGRAM_LISTS);
		assertThat(actualFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verifyNoMoreInteractions(this.programValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, null);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);

	}

	@Test
	public void shouldMoveGermplasmListFromCropListToProgramListChildFolder() {
		final String folderName = "folderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getParentId()).thenReturn(null);
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(null);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, null))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, null, null))
			.thenReturn(Optional.empty());
		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, null, null))
			.thenReturn(folderId);

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.isFolder()).thenReturn(true);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(parentFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer actualFolderId = this.germplasmListService
			.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
		assertThat(actualFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verifyNoMoreInteractions(this.programValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(newParentId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, null);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailMoveGermplasmListIfCurrentFolderNotExists() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.empty());

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(newParentId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListIfCurrentFolderHasChildren() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(
			Collections.singletonList(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.move.folder.has.child"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(newParentId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
	}

	@Test
	public void shouldFailMoveGermplasmListIfParentIsNotFolder() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.isFolder()).thenReturn(false);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(parentFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.move.list.another.list.not.allowed"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(newParentId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListIfParentHasAlreadyFolderWithSameName() {
		final String folderName = "folderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.isFolder()).thenReturn(true);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.name.exists"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(newParentId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);

		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
	}

	@Test
	public void shouldDeleteGermplasmListFolder() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(true);
		Mockito.when(actualFolder.getUserId()).thenReturn(USER_ID);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		Mockito.doNothing().when(this.germplasmListServiceMiddleware).deleteGermplasmListFolder(folderId);

		this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).deleteGermplasmListFolder(folderId);
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
		Mockito.verifyNoMoreInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfFolderIsCropList() {

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, GermplasmListServiceImpl.CROP_LISTS);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoInteractions(this.germplasmListManager);
		Mockito.verifyNoInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfFolderIsProgramList() {

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, GermplasmListServiceImpl.PROGRAM_LISTS);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoInteractions(this.germplasmListManager);
		Mockito.verifyNoInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderWithNullFolderId() {

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, null);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoInteractions(this.germplasmListManager);
		Mockito.verifyNoInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfNotFolder() {

		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(false);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.delete.not.folder"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);

		Mockito.verifyNoInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfFolderHasChildren() {

		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(true);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID))
			.thenReturn(Collections.singletonList(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.delete.folder.has.child"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);

		Mockito.verifyNoInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfUserIsNotTheOwner() {

		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(true);
		Mockito.when(actualFolder.getUserId()).thenReturn(USER_ID + 1);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID))
			.thenReturn(new ArrayList<>());

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.delete.not.owner"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
		Mockito.verifyNoMoreInteractions(this.securityService);
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
	public void testGetUserTreeState_NoSavedTreeState() {
		final String userId = RandomStringUtils.randomNumeric(3);
		Mockito.doReturn(Collections.emptyList()).when(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());

		final List<TreeNode> treeNodes = this.germplasmListService
			.getUserTreeState(GermplasmListServiceImplTest.CROP, GermplasmListServiceImplTest.PROGRAM_UUID, userId);
		final ArgumentCaptor<ProgramDTO> programCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(any(), eq(userId));
		Mockito.verify(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());
		Assert.assertThat(treeNodes.size(), is(2));
		Assert.assertThat(treeNodes.get(0).getKey(), is(GermplasmListServiceImpl.CROP_LISTS));
		Assert.assertTrue(treeNodes.get(0).getChildren().isEmpty());
		Assert.assertThat(treeNodes.get(1).getKey(), is(GermplasmListServiceImpl.PROGRAM_LISTS));
		Assert.assertTrue(treeNodes.get(1).getChildren().isEmpty());
	}

	@Test
	public void testGetUserTreeState_WithSavedTreeState() {
		final String userId = RandomStringUtils.randomNumeric(3);
		Mockito.doReturn(Arrays.asList("Program Lists", " 2", " 4", " 5")).when(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());
		// Test Tree looks like this:
		//   > "Program Lists"
		//      v 2
		//         v 4
		//           > 6
		//           > 7
		//        v 5
		//           > 8
		//      > 3
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(2), this.getGermplasmList(3))).when(this.germplasmListManager)
			.getAllTopLevelLists(GermplasmListServiceImplTest.PROGRAM_UUID);
		// Folder IDs 4 and 5 under parent 2
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(4), this.getGermplasmList(5))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderIdBatched(2, GermplasmListServiceImplTest.PROGRAM_UUID, GermplasmListServiceImpl.BATCH_SIZE);
		// Folder IDs 6 and 7 under parent 4
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(6), this.getGermplasmList(7))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderIdBatched(4, GermplasmListServiceImplTest.PROGRAM_UUID, GermplasmListServiceImpl.BATCH_SIZE);
		Mockito.doReturn(Collections.singletonList(this.getGermplasmList(8))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderIdBatched(5, GermplasmListServiceImplTest.PROGRAM_UUID, GermplasmListServiceImpl.BATCH_SIZE);
		Mockito.doReturn(Optional.of(this.getGermplasmList(new Random().nextInt()))).when(this.germplasmListServiceMiddleware)
			.getGermplasmListById(any());

		final List<TreeNode> treeNodes = this.germplasmListService
			.getUserTreeState(GermplasmListServiceImplTest.CROP, GermplasmListServiceImplTest.PROGRAM_UUID, userId);
		final ArgumentCaptor<ProgramDTO> programCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(any(), eq(userId));
		Mockito.verify(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());
		Assert.assertThat(treeNodes.size(), is(2));
		// Verify root Crop and Program Nodes
		Assert.assertThat(treeNodes.get(0).getKey(), is(GermplasmListServiceImpl.CROP_LISTS));
		Assert.assertTrue(treeNodes.get(0).getChildren().isEmpty());
		Assert.assertThat(treeNodes.get(1).getKey(), is(GermplasmListServiceImpl.PROGRAM_LISTS));
		// Verify children of root Program Lists node
		Assert.assertThat(treeNodes.get(1).getChildren().size(), is(2));
		final TreeNode folderID2 = treeNodes.get(1).getChildren().get(0);
		Assert.assertThat(folderID2.getKey(), is("2"));
		final TreeNode folderId3 = treeNodes.get(1).getChildren().get(1);
		Assert.assertThat(folderId3.getKey(), is("3"));
		Assert.assertNull(folderId3.getChildren());

		// Verify children of Program Lists > Folder ID 2
		Assert.assertThat(folderID2.getChildren().size(), is(2));
		final TreeNode folderId4 = folderID2.getChildren().get(0);
		Assert.assertThat(folderId4.getKey(), is("4"));
		final TreeNode folderId5 = folderID2.getChildren().get(1);
		Assert.assertThat(folderId5.getKey(), is("5"));

		// Verify children of Program Lists > Folder ID 2 > Folder ID 4
		Assert.assertThat(folderId4.getChildren().size(), is(2));
		final TreeNode folderId6 = folderId4.getChildren().get(0);
		Assert.assertThat(folderId6.getKey(), is("6"));
		Assert.assertNull(folderId6.getChildren());
		final TreeNode folderId7 = folderId4.getChildren().get(1);
		Assert.assertThat(folderId7.getKey(), is("7"));
		Assert.assertNull(folderId7.getChildren());

		// Verify children of Program Lists > Folder ID 2 > Folder ID 5
		Assert.assertThat(folderId5.getChildren().size(), is(1));
		final TreeNode folderId8 = folderId5.getChildren().get(0);
		Assert.assertThat(folderId8.getKey(), is("8"));
		Assert.assertNull(folderId8.getChildren());
	}

	@Test
	public void testSaveTreeState_ValidInputs() {
		Mockito.doReturn(Optional.of(this.getGermplasmList(new Random().nextInt()))).when(this.germplasmListServiceMiddleware)
			.getGermplasmListById(any());
		final String userId = org.apache.commons.lang.RandomStringUtils.randomNumeric(2);
		final UserTreeState treeState = new UserTreeState();
		treeState.setUserId(userId);
		treeState.setFolders(Lists.newArrayList(GermplasmListServiceImpl.PROGRAM_LISTS, "5", "7"));

		this.germplasmListService
			.saveGermplasmListTreeState(GermplasmListServiceImplTest.CROP, GermplasmListServiceImplTest.PROGRAM_UUID, treeState);
		final ArgumentCaptor<ProgramDTO> programCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(any(), eq(userId));
		Mockito.verify(this.userProgramStateDataManager)
			.saveOrUpdateUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST
					.name(), treeState.getFolders());
	}

	@Test
	public void testSaveTreeState_FolderDoesntExistInProgram() {
		final String invalidFolder = "7";
		Mockito.doReturn(Optional.empty()).when(this.germplasmListServiceMiddleware)
			.getGermplasmListById(Integer.parseInt(invalidFolder));
		final String validFolder = "5";
		Mockito.doReturn(Optional.of(this.getGermplasmList(Integer.parseInt(validFolder)))).when(this.germplasmListServiceMiddleware)
			.getGermplasmListByIdAndProgramUUID(Integer.parseInt(validFolder), GermplasmListServiceImplTest.PROGRAM_UUID);
		final String userId = org.apache.commons.lang.RandomStringUtils.randomNumeric(2);
		final UserTreeState treeState = new UserTreeState();
		treeState.setUserId(userId);
		treeState.setFolders(Lists.newArrayList(GermplasmListServiceImpl.PROGRAM_LISTS, validFolder, invalidFolder));

		try {
			this.germplasmListService
				.saveGermplasmListTreeState(GermplasmListServiceImplTest.CROP, GermplasmListServiceImplTest.PROGRAM_UUID, treeState);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(e.getErrors().get(0).getCode(), is("list.folder.id.not.exist"));
			Mockito.verify(this.userProgramStateDataManager, Mockito.never())
				.saveOrUpdateUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
					ListTreeState.GERMPLASM_LIST
						.name(), treeState.getFolders());
		}
	}

	@Test
	public void testSaveTreeState_NoFolderToSave() {
		final String userId = org.apache.commons.lang.RandomStringUtils.randomNumeric(2);
		final UserTreeState treeState = new UserTreeState();
		treeState.setUserId(userId);

		try {
			this.germplasmListService
				.saveGermplasmListTreeState(GermplasmListServiceImplTest.CROP, GermplasmListServiceImplTest.PROGRAM_UUID, treeState);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(e.getErrors().get(0).getCode(), is("list.folders.empty"));
			Mockito.verify(this.userProgramStateDataManager, Mockito.never())
				.saveOrUpdateUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
					ListTreeState.GERMPLASM_LIST
						.name(), treeState.getFolders());
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
		Mockito.when(germplasmListGeneratorDTO.getId()).thenReturn(GERMPLASM_LIST_ID);

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
		Mockito.when(germplasmListGeneratorDTO.getId()).thenReturn(GERMPLASM_LIST_ID);
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
		Mockito.when(germplasmListGeneratorDTO.getId()).thenReturn(GERMPLASM_LIST_ID);

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

	private void createMockListEntries(final GermplasmListGeneratorDTO germplasmListGeneratorDTO, final long expectedEntriesCount) {
		final GermplasmListGeneratorDTO.GermplasmEntryDTO entry1 = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
		entry1.setEntryNo(1);
		final GermplasmListGeneratorDTO.GermplasmEntryDTO entry2 = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
		entry2.setEntryNo(2);
		Mockito.when(germplasmListGeneratorDTO.getEntries()).thenReturn(Arrays.asList(entry1, entry2));

		Mockito.when(this.germplasmListDataService.countSearchGermplasmListData(germplasmListGeneratorDTO.getId(),
			new GermplasmListDataSearchRequest())).thenReturn(expectedEntriesCount);
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
		this.germplasmListService.clone(GERMPLASM_LIST_ID, request);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListMetadata(request, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateParentFolder(request);
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
		list.setName(RandomStringUtils.random(50));
		list.setDescription(RandomStringUtils.random(255));
		list.setDate(new Date());
		list.setType(GERMPLASM_LIST_TYPE);
		list.setParentFolderId(GermplasmListServiceImpl.PROGRAM_LISTS);
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
		list.setParentFolderId(GermplasmListServiceImpl.PROGRAM_LISTS);
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

}
