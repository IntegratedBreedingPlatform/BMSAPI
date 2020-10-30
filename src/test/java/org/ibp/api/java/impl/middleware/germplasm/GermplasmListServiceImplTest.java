package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.Matchers;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.times;

public class GermplasmListServiceImplTest {

	private static final String GERMPLASM_LIST_TYPE = "LST";
	private static final int GID1 = 1;
	private static final int GID2 = 2;
	private final String PROGRAM_UUID = org.apache.commons.lang.RandomStringUtils.random(20);

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
	public GermplasmListService germplasmListServiceMiddleware;

	@InjectMocks
	private GermplasmListServiceImpl germplamListService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ContextHolder.setCurrentProgram(this.PROGRAM_UUID);

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode(GERMPLASM_LIST_TYPE);
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("GERMPLASM LISTS");

		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Arrays.asList(userDefinedField));
		final List<Germplasm> germplasms = new ArrayList<>();
		germplasms.add(new Germplasm(GID1));
		germplasms.add(new Germplasm(GID2));
		Mockito.when(this.germplasmDataManager.getGermplasms(ArgumentMatchers.anyList())).thenReturn(germplasms);
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
		germplamListService.getGermplasmListChildrenNodes("maize", null, GermplasmListServiceImpl.PROGRAM_LISTS, Boolean.FALSE);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testGetGermplasmListChildrenNodes_InvalidFolderId_ThrowsException() throws ApiRequestValidationException {
		final String parentId = "1";
		final GermplasmList germplasmList = new GermplasmList();
		final String program = RandomStringUtils.randomAlphabetic(3);
		Mockito.when(germplasmListManager.getGermplasmListById(Integer.parseInt(parentId))).thenReturn(germplasmList);
		germplamListService.getGermplasmListChildrenNodes("maize", program, parentId, Boolean.FALSE);

	}

	@Test
	public void testGetGermplasmListChildrenNodes_NoProgramSpecified_ReturnOnlyCropFolder() throws ApiRequestValidationException {
		final List<TreeNode> result = germplamListService.getGermplasmListChildrenNodes("maize", null, null, Boolean.FALSE);
		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(GermplasmListServiceImpl.CROP_LISTS, result.get(0).getKey());
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ProgramIsSpecified_ReturnCropAndProgramFolder() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);

		final List<TreeNode> result = germplamListService.getGermplasmListChildrenNodes("maize", program, null, Boolean.FALSE);
		Assert.assertEquals(result.size(), 2);
		Assert.assertEquals(GermplasmListServiceImpl.CROP_LISTS, result.get(0).getKey());
		Assert.assertEquals(GermplasmListServiceImpl.PROGRAM_LISTS, result.get(1).getKey());

	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsCropList_LoadCropGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		germplamListService.getGermplasmListChildrenNodes("maize", program, GermplasmListServiceImpl.CROP_LISTS, Boolean.FALSE);
		Mockito.verify(germplasmListManager, times(1)).getAllTopLevelLists(null);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsProgramList_LoadProgramGermplasmLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		germplamListService.getGermplasmListChildrenNodes("maize", program, GermplasmListServiceImpl.PROGRAM_LISTS, Boolean.FALSE);
		Mockito.verify(germplasmListManager, times(1)).getAllTopLevelLists(program);
	}

	@Test
	public void testGetGermplasmListChildrenNodes_ParentIsAFolder_LoadLists() throws ApiRequestValidationException {
		final String program = RandomStringUtils.randomAlphabetic(3);
		final String parentId = "1";

		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(GermplasmList.FOLDER_TYPE);

		Mockito.when(germplasmListManager.getGermplasmListById(Integer.parseInt(parentId))).thenReturn(germplasmList);

		germplamListService.getGermplasmListChildrenNodes("maize", program, parentId, Boolean.FALSE);
		Mockito.verify(germplasmListManager, times(1)).getGermplasmListByParentFolderIdBatched(Integer.parseInt(parentId), program, GermplasmListServiceImpl.BATCH_SIZE);
	}

	@Test
	public void shouldGetGermplasmListTypes() {

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode("CHECK");
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("CHECK LIST");

		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Arrays.asList(userDefinedField));

		List<GermplasmListTypeDTO> germplasmListTypes = this.germplamListService.getGermplasmListTypes();
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
			this.germplamListService.create(request);
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
			this.germplamListService.create(request);
			Assert.fail();
		} catch (final ApiValidationException e) {
			Assert.assertThat(e.getErrorCode(), is("error.germplasmlist.save.entryno.gaps"));
		}
	}

	@Test
	public void testCreate_ShouldHaveAutoGeneratedEntryNo() {
		final GermplasmListGeneratorDTO request = this.createGermplasmList();
		this.germplamListService.create(request);
		Assert.assertThat(request.getEntries().get(1).getEntryNo(), is(2));
	}

	@Test
	public void testCreate_ShouldValidateEntryCode() {
		try {
			final GermplasmListGeneratorDTO request = this.createGermplasmList();
			request.getEntries().get(1).setEntryCode("Entry2");
			this.germplamListService.create(request);
			Assert.fail();
		} catch (final ApiValidationException e) {
			Assert.assertThat(e.getErrorCode(), is("error.germplasmlist.save.gaps"));
			Assert.assertThat(e.getParams()[0], is(GermplasmListServiceImpl.ENTRY_CODE));
		}
	}

	@Test
	public void testCreate_ShouldHaveAutoGeneratedEntryCode() {
		final GermplasmListGeneratorDTO request = this.createGermplasmList();
		this.germplamListService.create(request);
		Assert.assertThat(request.getEntries().get(1).getEntryCode(), is(String.valueOf(request.getEntries().get(1).getEntryNo())));
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
		entries.add(entry1);
		final GermplasmListGeneratorDTO.GermplasmEntryDTO entry2 = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
		entry2.setGid(GID2);
		entries.add(entry2);
		list.setEntries(entries);
		return list;
	}

}
