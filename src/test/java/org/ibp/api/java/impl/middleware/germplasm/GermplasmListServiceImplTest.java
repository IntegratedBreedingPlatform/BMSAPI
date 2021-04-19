package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.dao.GermplasmListDataDAO;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
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
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;

public class GermplasmListServiceImplTest {

	private static final String GERMPLASM_LIST_TYPE = "LST";
	private static final int GID1 = 1;
	private static final int GID2 = 2;
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();
	private static final Integer USER_ID = new Random().nextInt();
	private static final String CROP = "maize";

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
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private UserValidator userValidator;

	@Mock
	private UserProgramStateDataManager userProgramStateDataManager;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ContextHolder.setCurrentProgram(PROGRAM_UUID);

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode(GERMPLASM_LIST_TYPE);
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("GERMPLASM LISTS");

		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Arrays.asList(userDefinedField));
		final List<Germplasm> germplasms = new ArrayList<>();
		germplasms.add(new Germplasm(GID1));
		germplasms.add(new Germplasm(GID2));
		Mockito.when(this.germplasmDataManager.getGermplasms(ArgumentMatchers.anyList())).thenReturn(germplasms);

		final HashMap<Integer, String> plotCodeValuesByGids = new HashMap<>();
		plotCodeValuesByGids.put(GID1, GermplasmListDataDAO.SOURCE_UNKNOWN);
		plotCodeValuesByGids.put(GID2, GermplasmListDataDAO.SOURCE_UNKNOWN);
		Mockito.when(this.germplasmService.getPlotCodeValues(ArgumentMatchers.anySet())).thenReturn(plotCodeValuesByGids);
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
	public void testGetGermplasmListChildrenNodes_ProgramNotSpecified_ThrowsException() throws ApiRequestValidationException {
		this.germplasmListService.getGermplasmListChildrenNodes(CROP, null, GermplasmListServiceImpl.PROGRAM_LISTS, Boolean.FALSE);

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
		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(GermplasmListServiceImpl.CROP_LISTS, result.get(0).getKey());
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ProgramIsSpecified_ReturnCropAndProgramFolder() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);

		final List<TreeNode> result = this.germplasmListService.getGermplasmListChildrenNodes(CROP, program, null, Boolean.FALSE);
		Assert.assertEquals(result.size(), 2);
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

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(Integer.parseInt(parentId), program)).thenReturn(Optional.of(germplasmList));

		this.germplasmListService.getGermplasmListChildrenNodes(CROP, program, parentId, Boolean.FALSE);
		Mockito.verify(this.germplasmListManager, times(1)).getGermplasmListByParentFolderIdBatched(Integer.parseInt(parentId), program, GermplasmListServiceImpl.BATCH_SIZE);
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

		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Arrays.asList(userDefinedField));

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
	public void shouldCreateGermplasmListFolder() {

		final String folderName = "newFolderName";
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newFolderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(parentId, PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));
		Mockito.when(this.germplasmListServiceMiddleware.createGermplasmListFolder(USER_ID, folderName, parentId, PROGRAM_UUID))
			.thenReturn(newFolderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, String.valueOf(parentId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(newFolderId));

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(parentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).createGermplasmListFolder(USER_ID, folderName, parentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldCreateGermplasmListFolderWithProgramListAsParent() {

		final String folderName = StringUtils.repeat("a", GermplasmListServiceImpl.NAME_MAX_LENGTH);
		final String parentId = GermplasmListServiceImpl.PROGRAM_LISTS;
		final Integer newFolderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));
		Mockito.when(this.germplasmListServiceMiddleware.createGermplasmListFolder(USER_ID, folderName, null, PROGRAM_UUID))
			.thenReturn(newFolderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, String.valueOf(parentId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(newFolderId));

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).createGermplasmListFolder(USER_ID, folderName, null, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailCreateGermplasmWithCropListAsParent() {

		final String folderName = "newFolderName";
		final String parentId = GermplasmListServiceImpl.CROP_LISTS;

		try {
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.parent.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailCreateGermplasmWithNullFolderName() {

		final String folderName = null;
		final String parentId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		try {
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.empty"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailCreateGermplasmWithEmptyFolderName() {

		final String folderName = "";
		final String parentId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		try {
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.empty"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailCreateGermplasmWithTooLongFolderName() {

		final String folderName = StringUtils.repeat("a", GermplasmListServiceImpl.NAME_MAX_LENGTH + 1);
		final String parentId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		try {
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.name.too.long"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailCreateGermplasmListFolderIfParentNotExists() {

		final String folderName = "newFolderName";
		final String parentId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(Integer.parseInt(parentId), PROGRAM_UUID))
			.thenReturn(Optional.empty());

		try {
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.parent.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(Integer.parseInt(parentId), PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailCreateGermplasmListFolderWithSameFolderNameInParent() {

		final String folderName = "newFolderName";
		final String parentId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(Integer.parseInt(parentId), PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, Integer.parseInt(parentId), PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));

		try {
			this.germplasmListService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.name.exists"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(Integer.parseInt(parentId), PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, Integer.parseInt(parentId), PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldUpdateGermplasmListFolder() {

		final String folderName = StringUtils.repeat("a", GermplasmListServiceImpl.NAME_MAX_LENGTH);
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getParentId()).thenReturn(parentId);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(germplasmList.isFolder()).thenReturn(true);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(germplasmList));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));
		Mockito.when(this.germplasmListServiceMiddleware.updateGermplasmListFolder(USER_ID, folderName, folderId, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, String.valueOf(folderId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).updateGermplasmListFolder(USER_ID, folderName, folderId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmIfFolderIsProgramList() {

		final String folderName = "newFolderName";
		final String folderId = GermplasmListServiceImpl.PROGRAM_LISTS;

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmIfFolderIsCropList() {

		final String folderName = "newFolderName";
		final String folderId = GermplasmListServiceImpl.CROP_LISTS;

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmListFolderWithNullFolderName() {

		final String folderName = null;
		final String folderId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.empty"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmListFolderWithEmptyFolderName() {

		final String folderName = "";
		final String folderId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.empty"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmListFolderWithTooLongFolderName() {

		final String folderName = StringUtils.repeat("a", GermplasmListServiceImpl.NAME_MAX_LENGTH + 1);
		final String parentId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, parentId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.name.too.long"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
	}


	@Test
	public void shouldFailUpdateGermplasmListFolderIfParentNotExists() {

		final String folderName = "newFolderName";
		final String folderId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(Integer.parseInt(folderId), PROGRAM_UUID))
			.thenReturn(Optional.empty());

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(Integer.parseInt(folderId), PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmListFolderIfNotFolder() {

		final String folderName = "newFolderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.isFolder()).thenReturn(false);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(germplasmList));

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, String.valueOf(folderId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmListFolderWithSameFolderNameInParent() {

		final String folderName = "newFolderName";
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getParentId()).thenReturn(parentId);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(germplasmList.isFolder()).thenReturn(true);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(germplasmListId, PROGRAM_UUID))
			.thenReturn(Optional.of(germplasmList));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(germplasmListId))
			.thenReturn(Optional.of(germplasmList));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, String.valueOf(germplasmListId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.name.exists"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(germplasmListId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(germplasmListId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID);
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

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

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

		final Integer germplasmListFolderId =
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID);
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

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, null, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer germplasmListFolderId =
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), newParentId);
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
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
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListWithNullNewParentFolderId() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), null);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.parent.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListWithSameFolderIdAndNewParentId() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(folderId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.move.id.same.values"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmToListToCropList() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), GermplasmListServiceImpl.CROP_LISTS);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.parent.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListIfCurrentFolderNotExists() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.not.exist"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListIfCurrentFolderHasChildren() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(
			Arrays.asList(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.move.folder.has.child"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldFailMoveGermplasmListIfParentIsNotFolder() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newParentId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.isFolder()).thenReturn(false);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(parentFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.move.list.another.list.not.allowed"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID);
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
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.isFolder()).thenReturn(true);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(parentFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListService.moveGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.name.exists"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(newParentId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
	}

	@Test
	public void shouldDeleteGermplasmListFolder() {
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(true);
		Mockito.when(actualFolder.getUserId()).thenReturn(USER_ID);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID)).thenReturn(new ArrayList<>());

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));
		Mockito.doNothing().when(this.germplasmListServiceMiddleware).deleteGermplasmListFolder(folderId);

		this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
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
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
		Mockito.verifyZeroInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfFolderIsProgramList() {

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, GermplasmListServiceImpl.PROGRAM_LISTS);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
		Mockito.verifyZeroInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderWithNullFolderId() {

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, null);
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmListManager);
		Mockito.verifyZeroInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfNotFolder() {

		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(false);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.delete.not.folder"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);

		Mockito.verifyZeroInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfFolderHasChildren() {

		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(true);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID))
			.thenReturn(Arrays.asList(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.delete.folder.has.child"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);

		Mockito.verifyZeroInteractions(this.securityService);
	}

	@Test
	public void shouldFailDeleteGermplasmListFolderIfUserIsNotTheOwner() {

		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(true);
		Mockito.when(actualFolder.getUserId()).thenReturn(USER_ID + 1);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID))
			.thenReturn(new ArrayList<>());

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

		try {
			this.germplasmListService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));
			fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.delete.not.owner"));
		}

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListManager).getGermplasmListByParentFolderId(folderId, PROGRAM_UUID);
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);

		Mockito.verifyZeroInteractions(this.securityService);
	}

	@Test
	public void testAddGermplasmEntriesToList_WithSelectedItems_OK() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);
		Mockito.when(searchComposite.getItemIds()).thenReturn(new HashSet<>(Arrays.asList(new Random().nextInt())));

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.isFolder()).thenReturn(false);
		Mockito.when(germplasmList.isLockedList()).thenReturn(false);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(germplasmListId)).thenReturn(Optional.of(germplasmList));

		Mockito.doNothing().when(this.germplasmValidator).validateGids(ArgumentMatchers.any(MapBindingResult.class), ArgumentMatchers.anyList());

		this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(germplasmListId);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(MapBindingResult.class), ArgumentMatchers.anyList());

		Mockito.verify(this.germplasmListServiceMiddleware).addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testAddGermplasmEntriesToList_WithoutSelectedItems_OK() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.isFolder()).thenReturn(false);
		Mockito.when(germplasmList.isLockedList()).thenReturn(false);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(germplasmListId)).thenReturn(Optional.of(germplasmList));

		this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(germplasmListId);
		Mockito.verifyZeroInteractions(this.germplasmValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void testAddGermplasmEntriesToList_InvalidGermplasmListId_FAIL() {
		try {
			this.germplasmListService.addGermplasmEntriesToList(-1, Mockito.mock(SearchCompositeDto.class), PROGRAM_UUID);
			Assert.fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ResourceNotFoundException.class));
			MatcherAssert.assertThat(((ResourceNotFoundException) e).getError().getCode(), is("list.id.invalid"));
		}

		Mockito.verifyZeroInteractions(this.searchCompositeDtoValidator);
		Mockito.verifyZeroInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyZeroInteractions(this.germplasmValidator);
	}

	@Test
	public void testAddGermplasmEntriesToList_GermplasmListIdNotFound_FAIL() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(germplasmListId)).thenReturn(Optional.empty());

		try {
			this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);
			Assert.fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ResourceNotFoundException.class));
			MatcherAssert.assertThat(((ResourceNotFoundException) e).getError().getCode(), is("list.id.invalid"));
		}

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(germplasmListId);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);

		Mockito.verifyZeroInteractions(this.germplasmValidator);
	}

	@Test
	public void testAddGermplasmEntriesToList_GermplasmListIsFolder_FAIL() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.isFolder()).thenReturn(true);
		Mockito.when(germplasmList.isLockedList()).thenReturn(false);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(germplasmListId)).thenReturn(Optional.of(germplasmList));

		try {
			this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);
			Assert.fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.invalid"));
		}

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(germplasmListId);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);

		Mockito.verifyZeroInteractions(this.germplasmValidator);
	}

	@Test
	public void testAddGermplasmEntriesToList_GermplasmListIsLocked_FAIL() {
		final Integer germplasmListId = new Random().nextInt(Integer.MAX_VALUE);
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = Mockito.mock(SearchCompositeDto.class);

		Mockito.doNothing().when(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.isFolder()).thenReturn(false);
		Mockito.when(germplasmList.isLockedList()).thenReturn(true);
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(germplasmListId)).thenReturn(Optional.of(germplasmList));

		try {
			this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, PROGRAM_UUID);
			Assert.fail("Should has failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.locked"));
		}

		Mockito.verify(this.searchCompositeDtoValidator).validateSearchCompositeDto(ArgumentMatchers.eq(searchComposite),
			ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(germplasmListId);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);

		Mockito.verifyZeroInteractions(this.germplasmValidator);
	}

	@Test
	public void testGetGermplasmLists_WithNoErrors() {
		final Integer gid = 1;
		final GermplasmListDto dto = new GermplasmListDto();
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmLists(gid)).thenReturn(Collections.singletonList(dto));

		final List<GermplasmListDto> germplasmListDtos = this.germplasmListService.getGermplasmLists(gid);

		Assert.assertEquals(dto, germplasmListDtos.get(0));
	 	Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(gid)));
	 	Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmLists(gid);
	}

	@Test
	public void testGetGermplasmLists_ThrowsException_WhenGIDIsInvalid() {
		final Integer gid = 999;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(gid)));
			this.germplasmListService.getGermplasmLists(gid);
			Assert.fail("should throw an exception");
		} catch (ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(gid)));
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
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), ArgumentMatchers.any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(ArgumentMatchers.any(), ArgumentMatchers.eq(userId));
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
		Mockito.doReturn(Arrays.asList("Program Lists", " 2", " 4", " 6")).when(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());
		// Top level folder IDs under Program: 2, 3
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(2), this.getGermplasmList(3))).when(this.germplasmListManager)
			.getAllTopLevelLists(GermplasmListServiceImplTest.PROGRAM_UUID);
		// Folder IDs 4 and 5 under parent 2
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(4), this.getGermplasmList(5))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderIdBatched(2, GermplasmListServiceImplTest.PROGRAM_UUID, GermplasmListServiceImpl.BATCH_SIZE);
		// Folder IDs 6 and 7 under parent 4
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(6), this.getGermplasmList(7))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderIdBatched(4, GermplasmListServiceImplTest.PROGRAM_UUID, GermplasmListServiceImpl.BATCH_SIZE);
		Mockito.doReturn(Optional.of(this.getGermplasmList(new Random().nextInt()))).when(this.germplasmListServiceMiddleware)
			.getGermplasmListByIdAndProgramUUID(ArgumentMatchers.any(), ArgumentMatchers.any());

		final List<TreeNode> treeNodes = this.germplasmListService
			.getUserTreeState(GermplasmListServiceImplTest.CROP, GermplasmListServiceImplTest.PROGRAM_UUID, userId);
		final ArgumentCaptor<ProgramDTO> programCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), ArgumentMatchers.any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(ArgumentMatchers.any(), ArgumentMatchers.eq(userId));
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
		final TreeNode level1Folder = treeNodes.get(1).getChildren().get(0);
		Assert.assertThat(level1Folder.getKey(), is("2"));
		Assert.assertThat(treeNodes.get(1).getChildren().get(1).getKey(), is("3"));
		// Verify children of Program Lists > Folder ID 2
		Assert.assertThat(level1Folder.getChildren().size(), is(2));
		final TreeNode level2Folder = level1Folder.getChildren().get(0);
		Assert.assertThat(level2Folder.getKey(), is("4"));
		Assert.assertThat(level1Folder.getChildren().get(1).getKey(), is("5"));
		// Verify children of Program Lists > Folder ID 2 > Folder ID 4
		Assert.assertThat(level2Folder.getChildren().size(), is(2));
		final TreeNode level3Folder = level2Folder.getChildren().get(0);
		Assert.assertThat(level3Folder.getKey(), is("6"));
		Assert.assertTrue(level3Folder.getChildren().isEmpty());
		Assert.assertThat(level2Folder.getChildren().get(1).getKey(), is("7"));
		Assert.assertNull(level2Folder.getChildren().get(1).getChildren());
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

}
