package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.times;

public class GermplasmListServiceImplTest {

	@Autowired
	private GermplamListServiceImpl germplamListService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		germplamListService = new GermplamListServiceImpl();
		germplamListService.setWorkbenchDataManager(workbenchDataManager);
		germplamListService.setGermplasmListManager(germplasmListManager);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_MissingFolderOnly_ThrowsException() throws ApiRequestValidationException {
		germplamListService.getGermplasmListChildrenNodes("maize", null, null, null);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_InvalidParentId_ThrowsException() throws ApiRequestValidationException {
		germplamListService.getGermplasmListChildrenNodes("maize", null, "X", Boolean.TRUE);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_ProgramNotSpecified_ThrowsException() throws ApiRequestValidationException {
		germplamListService.getGermplasmListChildrenNodes("maize", null, GermplamListServiceImpl.PROGRAM_LISTS, Boolean.FALSE);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_InvalidFolderId_ThrowsException() throws ApiRequestValidationException {
		final String parentId = "1";
		final GermplasmList germplasmList = new GermplasmList();
		final String program = RandomStringUtils.randomAlphabetic(3);
		Mockito.when(workbenchDataManager.getProjectByUuidAndCrop(program, "maize")).thenReturn(new Project());
		Mockito.when(germplasmListManager.getGermplasmListById(Integer.parseInt(parentId))).thenReturn(germplasmList);
		germplamListService.getGermplasmListChildrenNodes("maize", program, parentId, Boolean.FALSE);

	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetGermplasmListChildrenNodes_ProgramInvalid_ThrowsException() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		Mockito.when(workbenchDataManager.getProjectByUuidAndCrop(program, "maize")).thenReturn(null);
		germplamListService.getGermplasmListChildrenNodes("maize", program, GermplamListServiceImpl.PROGRAM_LISTS, Boolean.FALSE);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_NoProgramSpecified_ReturnOnlyCropFolder() throws ApiRequestValidationException {
		final List<TreeNode> result = germplamListService.getGermplasmListChildrenNodes("maize", null, null, Boolean.FALSE);
		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(GermplamListServiceImpl.CROP_LISTS, result.get(0).getKey());
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ProgramIsSpecified_ReturnCropAndProgramFolder() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		Mockito.when(workbenchDataManager.getProjectByUuidAndCrop(program, "maize")).thenReturn(new Project());

		final List<TreeNode> result = germplamListService.getGermplasmListChildrenNodes("maize", program, null, Boolean.FALSE);
		Assert.assertEquals(result.size(), 2);
		Assert.assertEquals(GermplamListServiceImpl.CROP_LISTS, result.get(0).getKey());
		Assert.assertEquals(GermplamListServiceImpl.PROGRAM_LISTS, result.get(1).getKey());

	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsCropList_LoadCropGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		Mockito.when(workbenchDataManager.getProjectByUuidAndCrop(program, "maize")).thenReturn(new Project());
		germplamListService.getGermplasmListChildrenNodes("maize", program, GermplamListServiceImpl.CROP_LISTS, Boolean.FALSE);
		Mockito.verify(germplasmListManager, times(1)).getAllTopLevelLists(null);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsProgramList_LoadProgramGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		Mockito.when(workbenchDataManager.getProjectByUuidAndCrop(program, "maize")).thenReturn(new Project());
		germplamListService.getGermplasmListChildrenNodes("maize", program, GermplamListServiceImpl.PROGRAM_LISTS, Boolean.FALSE);
		Mockito.verify(germplasmListManager, times(1)).getAllTopLevelLists(program);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsAFolder_LoadLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		final String parentId = "1";

		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(GermplasmList.FOLDER_TYPE);

		Mockito.when(workbenchDataManager.getProjectByUuidAndCrop(program, "maize")).thenReturn(new Project());
		Mockito.when(germplasmListManager.getGermplasmListById(Integer.parseInt(parentId))).thenReturn(germplasmList);

		germplamListService.getGermplasmListChildrenNodes("maize", program, parentId, Boolean.FALSE);
		Mockito.verify(germplasmListManager, times(1)).getGermplasmListByParentFolderIdBatched(Integer.parseInt(parentId), program, GermplamListServiceImpl.BATCH_SIZE);
	}

}
