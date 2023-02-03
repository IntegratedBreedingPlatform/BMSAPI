package org.ibp.api.rest.sample;

import junit.framework.Assert;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.enumeration.SampleListType;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.ObjectError;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class SampleListServiceImplTest {

	public static final String PROGRAM_UUID = "23487325-dwfkjfsfdsaf-32874829374";

	@Mock
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Mock
	private SampleListValidator sampleListValidator;

	@Mock
	private SampleValidator sampleValidator;

	@Mock
	private SecurityService securityService;

	@Mock
	private ProgramValidator programValidator;

	@InjectMocks
	private SampleListServiceImpl sampleListService;

	final Random random = new Random();

	@Before
	public void init() {

		final WorkbenchUser workbenchUser = new WorkbenchUser();
		workbenchUser.setName("User1");
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(workbenchUser);

	}

	@Test
	public void testMoveSampleListFolder() {

		final int folderId = 1;
		final int newParentId = 2;
		final boolean isCropList = false;

		final SampleList sampleList = new SampleList();
		sampleList.setHierarchy(new SampleList());
		sampleList.getHierarchy().setId(newParentId);
		Mockito.when(this.sampleListServiceMW.moveSampleList(folderId, newParentId, isCropList, PROGRAM_UUID)).thenReturn(sampleList);

		final Map<String, Object> result = this.sampleListService.moveSampleListFolder(folderId, newParentId, isCropList, PROGRAM_UUID);

		Mockito.verify(this.sampleListValidator).validateFolderIdAndProgram(folderId);
		Mockito.verify(this.sampleListValidator).validateFolderId(newParentId);
		Assert.assertEquals(String.valueOf(newParentId), result.get(SampleListServiceImpl.PARENT_ID));
	}

	@Test
	public void testImportSamplePlateInformationSuccess() {
		final int listId = 1;
		final List<SampleDTO> sampleDTOs = this.createSampleDto();
		try {
			this.sampleListService.importSamplePlateInformation(sampleDTOs, listId);
			Mockito.verify(this.sampleListServiceMW).updateSamplePlateInfo(Mockito.eq(listId), Mockito.anyMapOf(
				String.class, SamplePlateInfo.class));
			Mockito.verify(this.sampleValidator).validateSamplesForImportPlate(listId, sampleDTOs);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("ApiRequestValidationException should not be thrown.");
		}

	}

	@Test
	public void testImportSamplePlateInformationError() {
		final int listId = 1;
		final List<SampleDTO> sampleDTOs = this.createSampleDto();
		final ApiRequestValidationException exception = new ApiRequestValidationException(Arrays.asList(new ObjectError("", "")));
		Mockito.doThrow(exception).when(this.sampleValidator).validateSamplesForImportPlate(listId, sampleDTOs);

		try {
			this.sampleListService.importSamplePlateInformation(sampleDTOs, listId);
			Assert.fail("ApiRequestValidationException should be thrown.");
		} catch (final ApiRequestValidationException e) {
			//
		}

	}

	@Test
	public void testConvertToSamplePlateInfoMap() {
		final List<SampleDTO> sampleDTOs = this.createSampleDto();
		final Map<String, SamplePlateInfo> map = this.sampleListService.convertToSamplePlateInfoMap(sampleDTOs);
		Assert.assertEquals(2, map.size());
		Assert.assertEquals("TestValue-1", map.get("SampleId-1").getPlateId());
		Assert.assertEquals("TestValue-4", map.get("SampleId-1").getWell());
		Assert.assertEquals("TestValue-2", map.get("SampleId-2").getPlateId());
		Assert.assertEquals("TestValue-5", map.get("SampleId-2").getWell());

	}

	@Test
	public void testCreateSampleList() {

		final Integer listId = 1;
		final SampleListDto sampleListDto = this.createSampleListDto();
		final SampleList sampleList = new SampleList();
		sampleList.setId(listId);
		Mockito.when(this.sampleListServiceMW.createSampleList(Mockito.any(SampleListDTO.class))).thenReturn(sampleList);
		final Map<String, Object> result = this.sampleListService.createSampleList(sampleListDto);
		Mockito.verify(this.sampleListValidator).validateSampleList(sampleListDto);
		Assert.assertEquals(String.valueOf(listId), result.get(SampleListServiceImpl.ID));

	}

	@Test
	public void testTranslateToSampleListDto() throws ParseException {

		final SampleListDto sampleListDto = this.createSampleListDto();
		final SampleListDTO sampleListDTO = this.sampleListService.translateToSampleListDto(sampleListDto);

		Assert.assertEquals(sampleListDTO.getCreatedBy(), sampleListDto.getCreatedBy());
		Assert.assertEquals(sampleListDTO.getCropName(), sampleListDto.getCropName());
		Assert.assertEquals(sampleListDTO.getProgramUUID(), sampleListDto.getProgramUUID());
		Assert.assertEquals(sampleListDTO.getDescription(), sampleListDto.getDescription());
		Assert.assertEquals(sampleListDTO.getInstanceIds(), sampleListDto.getInstanceIds());
		Assert.assertEquals(sampleListDTO.getNotes(), sampleListDto.getNotes());
		Assert.assertEquals(
			sampleListDTO.getSamplingDate(),
			DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(sampleListDto.getSamplingDate()));
		Assert.assertEquals(
			sampleListDTO.getCreatedDate(),
			DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(sampleListDto.getCreatedDate()));
		Assert.assertEquals(sampleListDTO.getSelectionVariableId(), sampleListDto.getSelectionVariableId());
		Assert.assertEquals(sampleListDTO.getDatasetId(), sampleListDto.getDatasetId());
		Assert.assertEquals(sampleListDTO.getTakenBy(), sampleListDto.getTakenBy());
		Assert.assertEquals(sampleListDTO.getParentId(), sampleListDto.getParentId());
		Assert.assertEquals(sampleListDTO.getListName(), sampleListDto.getListName());

	}

	@Test
	public void testDeleteSampleListEntries() {
		final Integer listId = 1;
		final List<Integer> selectedEntries = Arrays.asList(1, 2);
		this.sampleListService.deleteSamples(listId, selectedEntries);
		Mockito.verify(this.sampleListValidator).validateSampleList(listId);
		Mockito.verify(this.sampleListValidator).verifySamplesExist(listId, selectedEntries);
		Mockito.verify(this.sampleListServiceMW).deleteSamples(listId, selectedEntries);
	}

	private List<SampleDTO> createSampleDto() {
		final List<SampleDTO> sampleDTOs = new ArrayList<>();
		SampleDTO sampleDTO = new SampleDTO();
		sampleDTO.setSampleBusinessKey("SampleId-1");
		sampleDTO.setPlateId("TestValue-1");
		sampleDTO.setWell("TestValue-4");
		sampleDTOs.add(sampleDTO);
		sampleDTO = new SampleDTO();
		sampleDTO.setSampleBusinessKey("SampleId-2");
		sampleDTO.setPlateId("TestValue-2");
		sampleDTO.setWell("TestValue-5");
		sampleDTOs.add(sampleDTO);
		return sampleDTOs;

	}

	private SampleListDto createSampleListDto() {

		final SampleListDto sampleListDto = new SampleListDto();
		sampleListDto.setCreatedBy("User1");
		sampleListDto.setCropName(RandomStringUtils.randomAlphabetic(10));
		sampleListDto.setProgramUUID(RandomStringUtils.randomAlphabetic(10));
		sampleListDto.setDescription(RandomStringUtils.randomAlphabetic(10));
		sampleListDto.setInstanceIds(new ArrayList<Integer>());
		sampleListDto.setNotes(RandomStringUtils.randomAlphabetic(10));
		sampleListDto.setSamplingDate("2019-01-01");
		sampleListDto.setCreatedDate("2019-02-02");
		sampleListDto.setSelectionVariableId(this.random.nextInt(100));
		sampleListDto.setDatasetId(this.random.nextInt(100));
		sampleListDto.setParentId(this.random.nextInt());
		sampleListDto.setListName(RandomStringUtils.randomAlphabetic(10));
		return sampleListDto;
	}

	@Test
	public void testGetSampleChildNodesForProgramLists() {

		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.SAMPLE_LIST);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();

		Mockito.when(sampleListServiceMW.getAllSampleTopLevelLists(PROGRAM_UUID)).thenReturn(sampleLists);
		Mockito.when(sampleListServiceMW.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleListService.getSampleListChildrenNodes( "maize", PROGRAM_UUID, SampleListServiceImpl.PROGRAM_LISTS, false);

		Mockito.verify(sampleListServiceMW).getAllSampleTopLevelLists(PROGRAM_UUID);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertFalse(node.getIsLazy());
		Assert.assertFalse(node.getIsFolder());

	}

	@Test
	public void testGetSampleChildNodesForCropLists() {

		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.SAMPLE_LIST);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();

		Mockito.when(sampleListServiceMW.getAllSampleTopLevelLists(null)).thenReturn(sampleLists);
		Mockito.when(sampleListServiceMW.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleListService.getSampleListChildrenNodes("maize", PROGRAM_UUID, SampleListServiceImpl.CROP_LISTS, false);

		Mockito.verify(sampleListServiceMW).getAllSampleTopLevelLists(null);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertFalse(node.getIsLazy());
		Assert.assertFalse(node.getIsFolder());

	}

	@Test
	public void testGetSampleChildNodesOfAFolder() {

		final String parentFolderId = "1111";
		final SampleList parentFolder = new SampleList();
		parentFolder.setId(Integer.valueOf(parentFolderId));
		parentFolder.setType(SampleListType.FOLDER);

		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.SAMPLE_LIST);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();

		Mockito.when(sampleListServiceMW.getSampleList(Integer.valueOf(parentFolderId))).thenReturn(parentFolder);
		Mockito.when(sampleListServiceMW.getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleListServiceImpl.BATCH_SIZE)).thenReturn(sampleLists);
		Mockito.when(sampleListServiceMW.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleListService.getSampleListChildrenNodes("maize", PROGRAM_UUID, parentFolderId, false);

		Mockito.verify(sampleListServiceMW).getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleListServiceImpl.BATCH_SIZE);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertFalse(node.getIsLazy());
		Assert.assertFalse(node.getIsFolder());

	}

	@Test
	public void testGetSampleChildNodesOfAFolderChildIsAFolder() {

		final String parentFolderId = "1111";
		final SampleList parentFolder = new SampleList();
		parentFolder.setId(Integer.valueOf(parentFolderId));
		parentFolder.setType(SampleListType.FOLDER);

		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.FOLDER);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();
		ListMetadata listMetadata = new ListMetadata();
		listMetadata.setNumberOfChildren(123);
		sampleListsMetaData.put(id, listMetadata);

		Mockito.when(sampleListServiceMW.getSampleList(Integer.valueOf(parentFolderId))).thenReturn(parentFolder);
		Mockito.when(sampleListServiceMW.getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleListServiceImpl.BATCH_SIZE)).thenReturn(sampleLists);
		Mockito.when(sampleListServiceMW.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleListService.getSampleListChildrenNodes("maize", PROGRAM_UUID, parentFolderId, false);

		Mockito.verify(sampleListServiceMW).getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleListServiceImpl.BATCH_SIZE);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertTrue(node.getIsLazy());
		Assert.assertTrue(node.getIsFolder());

	}

}
