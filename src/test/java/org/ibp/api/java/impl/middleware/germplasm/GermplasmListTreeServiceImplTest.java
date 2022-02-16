package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.dao.germplasmlist.GermplasmListDataDAO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

public class GermplasmListTreeServiceImplTest {

	private static final String GERMPLASM_LIST_TYPE = "LST";
	private static final int GID1 = 1;
	private static final int GID2 = 2;
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();
	private static final Integer USER_ID = new Random().nextInt();
	private static final String CROP = "maize";
	private WorkbenchUser loggedInUser;

	@InjectMocks
	private GermplasmListTreeServiceImpl germplasmListTreeService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ProgramValidator programValidator;

	@Mock
	public SecurityService securityService;

	@Mock
	private GermplasmListService germplasmListServiceMiddleware;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private UserValidator userValidator;

	@Mock
	private UserProgramStateDataManager userProgramStateDataManager;

	@Mock
	private GermplasmListValidator germplasmListValidator;

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
		this.germplasmListTreeService.getGermplasmListChildrenNodes(CROP, null, null, null);

	}

	@Test
	public void testGetGermplasmListChildrenNodes_NoProgramSpecified_ReturnOnlyCropFolder() throws ApiRequestValidationException {
		final List<TreeNode> result = this.germplasmListTreeService.getGermplasmListChildrenNodes(CROP, null, null, Boolean.FALSE);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(GermplasmListTreeServiceImpl.CROP_LISTS, result.get(0).getKey());
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ProgramIsSpecified_ReturnCropAndProgramFolder() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);

		final List<TreeNode> result = this.germplasmListTreeService.getGermplasmListChildrenNodes(CROP, program, null, Boolean.FALSE);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(GermplasmListTreeServiceImpl.CROP_LISTS, result.get(0).getKey());
		Assert.assertEquals(GermplasmListTreeServiceImpl.PROGRAM_LISTS, result.get(1).getKey());

	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsCropList_LoadCropGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		this.germplasmListTreeService.getGermplasmListChildrenNodes(CROP, program, GermplasmListTreeServiceImpl.CROP_LISTS, Boolean.FALSE);
		Mockito.verify(this.germplasmListManager, times(1)).getAllTopLevelLists(null);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsProgramList_LoadProgramGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		this.germplasmListTreeService.getGermplasmListChildrenNodes(CROP, program, GermplasmListTreeServiceImpl.PROGRAM_LISTS, Boolean.FALSE);
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

		this.germplasmListTreeService.getGermplasmListChildrenNodes(CROP, program, parentId, Boolean.FALSE);
		Mockito.verify(this.germplasmListManager, times(1))
			.getGermplasmListByParentFolderId(Integer.parseInt(parentId));
	}

	private GermplasmList getGermplasmList(final Integer id) {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(GermplasmList.FOLDER_TYPE);
		germplasmList.setProgramUUID(GermplasmListTreeServiceImplTest.PROGRAM_UUID);
		germplasmList.setId(id);
		return germplasmList;
	}

	@Test
	public void shouldCreateGermplasmListFolder() {

		final String folderName = "newFolderName";
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);
		final Integer newFolderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(germplasmList.isFolder()).thenReturn(Boolean.TRUE);

		Mockito.doNothing().when(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.doNothing().when(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.createGermplasmListFolder(USER_ID, folderName, parentId, PROGRAM_UUID))
			.thenReturn(newFolderId);

		final Integer germplasmListFolderId =
			this.germplasmListTreeService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, String.valueOf(parentId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(newFolderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.verify(this.germplasmListValidator).validateFolderId(parentId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT);
		Mockito.verify(this.germplasmListServiceMiddleware).createGermplasmListFolder(USER_ID, folderName, parentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldCreateGermplasmListFolderWithProgramListAsParent() {

		final String folderName = StringUtils.repeat("a", GermplasmListValidator.NAME_MAX_LENGTH);
		final String parentId = GermplasmListTreeServiceImpl.PROGRAM_LISTS;
		final Integer newFolderId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListValidator.validateFolderId(parentId, PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT))
			.thenReturn(Optional.of(parentFolder));
		Mockito.doNothing().when(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.doNothing().when(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, null, PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.createGermplasmListFolder(USER_ID, folderName, null, PROGRAM_UUID))
			.thenReturn(newFolderId);

		final Integer germplasmListFolderId =
			this.germplasmListTreeService.createGermplasmListFolder(CROP, PROGRAM_UUID, folderName, parentId);
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(newFolderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListValidator).validateFolderId(parentId, PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT);
		Mockito.verify(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.verify(this.germplasmListServiceMiddleware).createGermplasmListFolder(USER_ID, folderName, null, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
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
		Mockito.when(this.germplasmListValidator.validateFolderId(folderId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.FOLDER))
			.thenReturn(Optional.of(germplasmList));

		Mockito.doNothing().when(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.doNothing().when(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.updateGermplasmListFolder(folderName, folderId))
			.thenReturn(folderId);

		final Integer germplasmListFolderId =
			this.germplasmListTreeService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, String.valueOf(folderId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).updateGermplasmListFolder(folderName, folderId);
		Mockito.verify(this.germplasmListValidator).validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateFolderName(folderName);
		Mockito.verify(this.germplasmListValidator).validateFolderId(folderId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.FOLDER);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldFailUpdateGermplasmIfFolderIsProgramList() {

		final String folderName = "newFolderName";
		final String folderId = GermplasmListTreeServiceImpl.PROGRAM_LISTS;

		try {
			this.germplasmListTreeService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
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
		final String folderId = GermplasmListTreeServiceImpl.CROP_LISTS;

		try {
			this.germplasmListTreeService.updateGermplasmListFolderName(CROP, PROGRAM_UUID, folderName, folderId);
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

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId)).thenReturn(new ArrayList<>());

		final GermplasmList parentFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(parentFolder.isFolder()).thenReturn(true);
		Mockito.when(parentFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(newParentId))
			.thenReturn(Optional.of(parentFolder));
		Mockito.when(this.germplasmListValidator.validateFolderId(newParentId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer germplasmListFolderId =
			this.germplasmListTreeService.moveGermplasmListNode(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListValidator).validateFolderId(newParentId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT);
		Mockito.verify(this.germplasmListValidator).validateNodeId(folderId.toString(), GermplasmListValidator.ListNodeType.FOLDER);
		Mockito.verify(this.germplasmListValidator).validateFolderHasNoChildren(folderId, "list.move.folder.has.child");
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
	}

	@Test
	public void shouldMoveGermplasmListFolderToProgramListAsParent() {
		final String folderName = "folderName";
		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);
		final String newParentId = GermplasmListTreeServiceImpl.PROGRAM_LISTS;

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.getParentId()).thenReturn(null);
		Mockito.when(actualFolder.getName()).thenReturn(folderName);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListById(folderId))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, null, PROGRAM_UUID))
			.thenReturn(folderId);

		Mockito.when(this.germplasmListValidator.validateFolderId(newParentId, PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT))
			.thenReturn(Optional.of(actualFolder));

		final Integer germplasmListFolderId =
			this.germplasmListTreeService.moveGermplasmListNode(CROP, PROGRAM_UUID, String.valueOf(folderId), newParentId);
		assertNotNull(germplasmListFolderId);
		assertThat(germplasmListFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListValidator).validateFolderId(newParentId, PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT);
		Mockito.verify(this.germplasmListValidator).validateFolderHasNoChildren(folderId, "list.move.folder.has.child");
		Mockito.verify(this.germplasmListValidator).validateNodeId(folderId.toString(), GermplasmListValidator.ListNodeType.FOLDER);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
	}

	@Test
	public void shouldFailMoveGermplasmListWithNullFolderId() {
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		try {
			this.germplasmListTreeService.moveGermplasmListNode(CROP, PROGRAM_UUID, null, String.valueOf(parentId));
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
			this.germplasmListTreeService.moveGermplasmListNode(CROP, PROGRAM_UUID, String.valueOf(folderId), null);
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
			this.germplasmListTreeService.moveGermplasmListNode(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(folderId));
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

		final Integer actualFolderId = this.germplasmListTreeService
			.moveGermplasmListNode(CROP, null, String.valueOf(folderId), GermplasmListTreeServiceImpl.CROP_LISTS);
		assertThat(actualFolderId, is(folderId));

		Mockito.verifyNoInteractions(this.programValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, null, null);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, null, null);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
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

		final Integer actualFolderId = this.germplasmListTreeService
			.moveGermplasmListNode(CROP, PROGRAM_UUID, String.valueOf(folderId), GermplasmListTreeServiceImpl.PROGRAM_LISTS);
		assertThat(actualFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verifyNoMoreInteractions(this.programValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, null, PROGRAM_UUID);
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
		Mockito.when(parentFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);


		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(parentFolder));
		Mockito.when(this.germplasmListValidator.validateFolderId(newParentId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		Mockito.when(this.germplasmListServiceMiddleware.moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID))
			.thenReturn(folderId);

		final Integer actualFolderId = this.germplasmListTreeService
			.moveGermplasmListNode(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
		assertThat(actualFolderId, is(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verifyNoMoreInteractions(this.programValidator);

		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListValidator).validateFolderId(newParentId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT);
		Mockito.verify(this.germplasmListValidator).validateNodeId(folderId.toString(), GermplasmListValidator.ListNodeType.FOLDER);
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID);
		Mockito.verify(this.germplasmListServiceMiddleware).moveGermplasmListFolder(folderId, newParentId, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
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
		Mockito.when(parentFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.when(this.germplasmListValidator.validateFolderId(newParentId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT))
			.thenReturn(Optional.of(parentFolder));

		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByParentAndName(folderName, newParentId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));

		try {
			this.germplasmListTreeService.moveGermplasmListNode(CROP, PROGRAM_UUID, String.valueOf(folderId), String.valueOf(newParentId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.name.exists"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListServiceMiddleware).getGermplasmListById(folderId);
		Mockito.verify(this.germplasmListValidator).validateFolderId(newParentId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.PARENT);
		Mockito.verify(this.germplasmListValidator).validateNodeId(String.valueOf(folderId), GermplasmListValidator.ListNodeType.FOLDER);
		Mockito.verify(this.germplasmListValidator).validateFolderHasNoChildren(folderId, "list.move.folder.has.child");

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
		Mockito.when(this.germplasmListValidator.validateFolderId(folderId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.FOLDER))
			.thenReturn(Optional.of(actualFolder));

		Mockito.doNothing().when(this.germplasmListServiceMiddleware).deleteGermplasmListFolder(folderId);

		this.germplasmListTreeService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListValidator).validateFolderId(folderId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.FOLDER);
		Mockito.verify(this.germplasmListValidator).validateFolderHasNoChildren(folderId, "list.delete.folder.has.child");
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
			this.germplasmListTreeService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, GermplasmListTreeServiceImpl.CROP_LISTS);
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
			this.germplasmListTreeService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, GermplasmListTreeServiceImpl.PROGRAM_LISTS);
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
			this.germplasmListTreeService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, null);
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
	public void shouldFailDeleteGermplasmListFolderIfUserIsNotTheOwner() {

		final Integer folderId = new Random().nextInt(Integer.MAX_VALUE);

		final GermplasmList actualFolder = Mockito.mock(GermplasmList.class);
		Mockito.when(actualFolder.isFolder()).thenReturn(true);
		Mockito.when(actualFolder.getUserId()).thenReturn(USER_ID + 1);
		Mockito.when(actualFolder.getProgramUUID()).thenReturn(PROGRAM_UUID);

		Mockito.doNothing().when(this.programValidator).validate(any(), any());
		Mockito.when(this.germplasmListServiceMiddleware.getGermplasmListByIdAndProgramUUID(folderId, PROGRAM_UUID))
			.thenReturn(Optional.of(actualFolder));
		Mockito.when(this.germplasmListValidator.validateFolderId(folderId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.FOLDER))
			.thenReturn(Optional.of(actualFolder));

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(folderId, PROGRAM_UUID))
			.thenReturn(new ArrayList<>());

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

		try {
			this.germplasmListTreeService.deleteGermplasmListFolder(CROP, PROGRAM_UUID, String.valueOf(folderId));
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(
				Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.delete.not.owner"));
		}

		Mockito.verify(this.programValidator).validate(any(), any());
		Mockito.verify(this.germplasmListValidator).validateFolderHasNoChildren(folderId, "list.delete.folder.has.child");
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
		Mockito.verify(this.germplasmListValidator).validateFolderId(folderId.toString(), PROGRAM_UUID, GermplasmListValidator.ListNodeType.FOLDER);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListServiceMiddleware);
		Mockito.verifyNoMoreInteractions(this.germplasmListManager);
		Mockito.verifyNoMoreInteractions(this.securityService);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
	}

	@Test
	public void testGetUserTreeState_NoSavedTreeState() {
		final String userId = RandomStringUtils.randomNumeric(3);
		Mockito.doReturn(Collections.emptyList()).when(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListTreeServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());

		final List<TreeNode> treeNodes = this.germplasmListTreeService
			.getUserTreeState(GermplasmListTreeServiceImplTest.CROP, GermplasmListTreeServiceImplTest.PROGRAM_UUID, userId);
		final ArgumentCaptor<ProgramDTO> programCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListTreeServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListTreeServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(any(), eq(userId));
		Mockito.verify(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListTreeServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());
		Assert.assertThat(treeNodes.size(), is(2));
		Assert.assertThat(treeNodes.get(0).getKey(), is(GermplasmListTreeServiceImpl.CROP_LISTS));
		Assert.assertTrue(treeNodes.get(0).getChildren().isEmpty());
		Assert.assertThat(treeNodes.get(1).getKey(), is(GermplasmListTreeServiceImpl.PROGRAM_LISTS));
		Assert.assertTrue(treeNodes.get(1).getChildren().isEmpty());
	}

	@Test
	public void testGetUserTreeState_WithSavedTreeState() {
		final String userId = RandomStringUtils.randomNumeric(3);
		Mockito.doReturn(Arrays.asList("Program Lists", " 2", " 4", " 5")).when(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListTreeServiceImplTest.PROGRAM_UUID,
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
			.getAllTopLevelLists(GermplasmListTreeServiceImplTest.PROGRAM_UUID);
		// Folder IDs 4 and 5 under parent 2
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(4), this.getGermplasmList(5))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderId(2);
		// Folder IDs 6 and 7 under parent 4
		Mockito.doReturn(Arrays.asList(this.getGermplasmList(6), this.getGermplasmList(7))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderId(4);
		Mockito.doReturn(Collections.singletonList(this.getGermplasmList(8))).when(this.germplasmListManager)
			.getGermplasmListByParentFolderId(5);
		Mockito.doReturn(Optional.of(this.getGermplasmList(new Random().nextInt()))).when(this.germplasmListServiceMiddleware)
			.getGermplasmListById(any());

		final List<TreeNode> treeNodes = this.germplasmListTreeService
			.getUserTreeState(GermplasmListTreeServiceImplTest.CROP, GermplasmListTreeServiceImplTest.PROGRAM_UUID, userId);
		final ArgumentCaptor<ProgramDTO> programCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListTreeServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListTreeServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(any(), eq(userId));
		Mockito.verify(this.userProgramStateDataManager)
			.getUserProgramTreeState(Integer.parseInt(userId), GermplasmListTreeServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());
		Assert.assertThat(treeNodes.size(), is(2));
		// Verify root Crop and Program Nodes
		Assert.assertThat(treeNodes.get(0).getKey(), is(GermplasmListTreeServiceImpl.CROP_LISTS));
		Assert.assertTrue(treeNodes.get(0).getChildren().isEmpty());
		Assert.assertThat(treeNodes.get(1).getKey(), is(GermplasmListTreeServiceImpl.PROGRAM_LISTS));
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
		treeState.setProgramFolders(Lists.newArrayList(GermplasmListTreeServiceImpl.PROGRAM_LISTS, "5", "7"));
		treeState.setCropFolders(Lists.newArrayList(GermplasmListTreeServiceImpl.CROP_LISTS, "15", "17"));

		this.germplasmListTreeService
			.saveGermplasmListTreeState(GermplasmListTreeServiceImplTest.CROP, GermplasmListTreeServiceImplTest.PROGRAM_UUID, treeState);
		final ArgumentCaptor<ProgramDTO> programCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
		Mockito.verify(this.programValidator).validate(programCaptor.capture(), any());
		Assert.assertThat(programCaptor.getValue().getCrop(), is(GermplasmListTreeServiceImplTest.CROP));
		Assert.assertThat(programCaptor.getValue().getUniqueID(), is(GermplasmListTreeServiceImplTest.PROGRAM_UUID));
		Mockito.verify(this.userValidator).validateUserId(any(), eq(userId));
		Mockito.verify(this.userProgramStateDataManager)
			.saveOrUpdateUserProgramTreeState(Integer.parseInt(userId), GermplasmListTreeServiceImplTest.PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST
					.name(), treeState.getProgramFolders());
	}

	@Test
	public void testSaveTreeState_NoProgramFolderToSave() {
		final String userId = org.apache.commons.lang.RandomStringUtils.randomNumeric(2);
		final UserTreeState treeState = new UserTreeState();
		treeState.setUserId(userId);
		treeState.setCropFolders(Lists.newArrayList(GermplasmListTreeServiceImpl.CROP_LISTS, "15", "17"));

		try {
			this.germplasmListTreeService
				.saveGermplasmListTreeState(GermplasmListTreeServiceImplTest.CROP, GermplasmListTreeServiceImplTest.PROGRAM_UUID, treeState);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(e.getErrors().get(0).getCode(), is("list.folders.empty"));
			Mockito.verify(this.userProgramStateDataManager, Mockito.never())
				.saveOrUpdateUserProgramTreeState(Integer.parseInt(userId), GermplasmListTreeServiceImplTest.PROGRAM_UUID,
					ListTreeState.GERMPLASM_LIST
						.name(), treeState.getProgramFolders());
		}
	}

	@Test
	public void testSaveTreeState_NoCropFolderToSave() {
		Mockito.doReturn(Optional.of(this.getGermplasmList(new Random().nextInt()))).when(this.germplasmListServiceMiddleware)
			.getGermplasmListById(any());
		final String userId = org.apache.commons.lang.RandomStringUtils.randomNumeric(2);
		final UserTreeState treeState = new UserTreeState();
		treeState.setUserId(userId);
		treeState.setProgramFolders(Lists.newArrayList(GermplasmListTreeServiceImpl.PROGRAM_LISTS, "15", "17"));

		try {
			this.germplasmListTreeService
				.saveGermplasmListTreeState(GermplasmListTreeServiceImplTest.CROP, GermplasmListTreeServiceImplTest.PROGRAM_UUID, treeState);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(e.getErrors().get(0).getCode(), is("list.folders.empty"));
			Mockito.verify(this.userProgramStateDataManager, Mockito.never())
				.saveOrUpdateUserProgramTreeState(Integer.parseInt(userId), GermplasmListTreeServiceImplTest.PROGRAM_UUID,
					ListTreeState.GERMPLASM_LIST
						.name(), treeState.getProgramFolders());
		}
	}

}
