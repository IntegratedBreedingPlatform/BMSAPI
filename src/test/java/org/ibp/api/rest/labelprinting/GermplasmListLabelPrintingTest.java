package org.ibp.api.rest.labelprinting;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListStaticColumns;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.data.initializer.PersonTestDataInitializer;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.GermplasmListDataDetail;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmListDataService;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListLabelPrintingTest {

	final static Integer LIST_ID = 1;
	final static String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);
	final static String GERMPLASM_DETAILS = "Germplasm Details";
	final static String ENTRY_DETAILS = "Entry Details";
	final static String NAMES = "Names";
	final static String ATTRIBUTES = "Attributes";
	final static Integer GID = 1;

	@Mock
	private GermplasmListService germplasmListService;

	@Mock
	private GermplasmListDataService germplasmListDataService;

	@Mock
	private UserService userService;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmSearchService germplasmSearchService;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private GermplasmAttributeService germplasmAttributeService;

	@Mock
	private GermplasmNameTypeService germplasmNameTypeService;

	@Mock
	private GermplasmNameService germplasmNameService;

	@InjectMocks
	private GermplasmListLabelPrinting labelPrinting;

	private LabelsInfoInput labelsInfoInput;
	private GermplasmSearchRequest germplasmSearchRequest;

	@Before
	public void setUp() {
		this.labelsInfoInput = new LabelsInfoInput();
		this.labelsInfoInput.setListId(LIST_ID);

		this.labelPrinting.setMaxTotalResults(3000);

		this.germplasmSearchRequest = new GermplasmSearchRequest();
		this.germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(LIST_ID));

		Mockito.when(this.messageSource.getMessage("label.printing.germplasm.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(GERMPLASM_DETAILS);
		Mockito.when(this.messageSource.getMessage("label.printing.entry.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(ENTRY_DETAILS);
		Mockito.when(this.messageSource.getMessage("label.printing.names.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(NAMES);
		Mockito.when(this.messageSource.getMessage("label.printing.attributes.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(ATTRIBUTES);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateLabelsInfoInputData() {
		Mockito.when(this.germplasmListDataService
			.countSearchGermplasmListData(this.labelsInfoInput.getListId(), new GermplasmListDataSearchRequest())).thenReturn(3001L);
		this.labelPrinting.validateLabelsInfoInputData(this.labelsInfoInput, PROGRAM_UUID);
	}

	@Test
	public void testGetOriginResourceMetadata() {
		final GermplasmListDto germplasmListDto = new GermplasmListDto();
		germplasmListDto.setOwnerId(1);
		germplasmListDto.setListName(RandomStringUtils.randomAlphanumeric(10));
		germplasmListDto.setDescription(RandomStringUtils.randomAlphanumeric(10));
		germplasmListDto.setCreationDate(new Date());
		Mockito.when(this.germplasmListService.getGermplasmListById(this.labelsInfoInput.getListId())).thenReturn(germplasmListDto);
		final long count = 50L;
		Mockito.when(this.germplasmListDataService
			.countSearchGermplasmListData(this.labelsInfoInput.getListId(), new GermplasmListDataSearchRequest())).thenReturn(count);
		final WorkbenchUser workbenchUser = new WorkbenchUser();
		workbenchUser.setPerson(PersonTestDataInitializer.createPerson());
		Mockito.when(this.userService.getUserById(germplasmListDto.getOwnerId())).thenReturn(workbenchUser);

		final String name = "name";
		Mockito.when(this.messageSource.getMessage("label.printing.list.name", null, LocaleContextHolder.getLocale()))
			.thenReturn(name);
		final String description = "description";
		Mockito.when(this.messageSource.getMessage("label.printing.description", null, LocaleContextHolder.getLocale()))
			.thenReturn(description);
		final String owner = "owner";
		Mockito.when(this.messageSource.getMessage("label.printing.owner", null, LocaleContextHolder.getLocale()))
			.thenReturn(owner);
		final String date = "date";
		Mockito.when(this.messageSource.getMessage("label.printing.date", null, LocaleContextHolder.getLocale()))
			.thenReturn(date);
		final String noOfEntries = "noOfEntries";
		Mockito.when(this.messageSource.getMessage("label.printing.noOfEntries", null, LocaleContextHolder.getLocale()))
			.thenReturn(noOfEntries);
		final OriginResourceMetadata originResourceMetadata =
			this.labelPrinting.getOriginResourceMetadata(this.labelsInfoInput, PROGRAM_UUID);
		Assert.assertTrue(originResourceMetadata.getDefaultFileName()
			.startsWith(GermplasmListLabelPrinting.LABELS_FOR.concat(germplasmListDto.getListName())));
		Assert.assertEquals(germplasmListDto.getListName(), originResourceMetadata.getMetadata().get(name));
		Assert.assertEquals(germplasmListDto.getDescription(), originResourceMetadata.getMetadata().get(description));
		Assert.assertEquals(workbenchUser.getPerson().getDisplayName(), originResourceMetadata.getMetadata().get(owner));
		Assert.assertEquals(
			Util.getSimpleDateFormat(Util.DATE_AS_NUMBER_FORMAT).format(germplasmListDto.getCreationDate()),
			originResourceMetadata.getMetadata().get(date));
		Assert.assertEquals(String.valueOf(count), originResourceMetadata.getMetadata().get(noOfEntries));
	}

	@Test
	public void testGetAvailableLabelTypes() {
		this.labelPrinting.initStaticFields();
		final List<Field> germplasmFields = new ArrayList<>(this.labelPrinting.getDefaultGermplasmDetailsFields());
		germplasmFields.addAll(this.labelPrinting.defaultPedigreeDetailsFields);

		final List<LabelType> labelTypes = this.labelPrinting.getAvailableLabelTypes(this.labelsInfoInput, PROGRAM_UUID);
		Mockito.verify(this.germplasmSearchService).searchGermplasm(ArgumentMatchers.any(GermplasmSearchRequest.class),
			ArgumentMatchers.eq(null), ArgumentMatchers.eq(PROGRAM_UUID));
		Mockito.verify(this.germplasmAttributeService, Mockito.never())
			.getGermplasmAttributeVariables(Collections.singletonList(GID), PROGRAM_UUID);
		Mockito.verify(this.germplasmNameTypeService, Mockito.never()).getNameTypesByGIDList(Collections.singletonList(GID));
		final Map<String, LabelType> labelTypeMap = labelTypes.stream().collect(Collectors.toMap(LabelType::getKey, Function.identity()));
		Assert.assertEquals(4, labelTypes.size());
		Assert.assertEquals(germplasmFields, labelTypeMap.get(GERMPLASM_DETAILS).getFields());
		Assert.assertTrue(CollectionUtils.isEmpty(labelTypeMap.get(NAMES).getFields()));
		Assert.assertTrue(CollectionUtils.isEmpty(labelTypeMap.get(ATTRIBUTES).getFields()));
	}

	@Test
	public void testGetLabelsData() {
		this.labelPrinting.initStaticFields();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setListId(LIST_ID);
		labelsGeneratorInput.setFields(new ArrayList<>());
		final LabelsData labelsData = this.labelPrinting.getLabelsData(labelsGeneratorInput, PROGRAM_UUID);
		Mockito.verify(this.germplasmService).searchGermplasm(this.germplasmSearchRequest, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmAttributeService, Mockito.never())
			.getGermplasmAttributeVariables(ArgumentMatchers.anyList(), ArgumentMatchers.anyString());
		Mockito.verify(this.germplasmNameService, Mockito.never()).getGermplasmNamesByGids(ArgumentMatchers.anyList());
		Mockito.verify(this.germplasmListDataService, Mockito.never()).getGermplasmListDataDetailList(LIST_ID);
		Mockito.verify(this.germplasmListDataService)
			.searchGermplasmListData(ArgumentMatchers.eq(LIST_ID), ArgumentMatchers.any(GermplasmListDataSearchRequest.class),
				ArgumentMatchers.any(PageRequest.class));
		Mockito.verify(this.germplasmListService, Mockito.never())
			.getGermplasmListVariables(PROGRAM_UUID, LIST_ID, VariableType.ENTRY_DETAIL.getId());
		Assert.assertEquals(LabelPrintingStaticField.GUID.getFieldId(), labelsData.getDefaultBarcodeKey());
		Assert.assertTrue(CollectionUtils.isEmpty(labelsData.getData()));
	}

	@Test
	public void testGetEntryDetailValues() {
		final Integer variableId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Integer listDataId = Integer.valueOf(RandomStringUtils.randomNumeric(3));
		final GermplasmListDataDetail detail = new GermplasmListDataDetail();
		detail.setListData(new GermplasmListData());
		detail.getListData().setId(listDataId);
		detail.setVariableId(variableId);
		detail.setValue(RandomStringUtils.randomAlphanumeric(10));
		Mockito.when(this.germplasmListDataService.getGermplasmListDataDetailList(LIST_ID)).thenReturn(Collections.singletonList(detail));

		final Map<Integer, Map<Integer, String>> entryDetailValues = new HashMap<>();
		this.labelPrinting.getEntryDetailValues(entryDetailValues, LIST_ID);
		Assert.assertTrue(entryDetailValues.containsKey(listDataId));
		Assert.assertEquals(detail.getValue(), entryDetailValues.get(listDataId).get(variableId));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields_WhenCSVFileType() {
		this.labelPrinting.initStaticFields();
		final Set<Integer> keys = new HashSet<>(Arrays.asList(TermId.GID.getId(), LabelPrintingStaticField.GUID.getFieldId(),
			TermId.PREFERRED_NAME.getId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setGermplasmPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.CSV);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, null, response, new HashMap<>(), new HashMap<>(), new HashMap<>());
		Assert.assertEquals(3, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(TermId.GID.getId()));
		Assert.assertEquals(response.getGermplasmUUID(), dataRow.get(LabelPrintingStaticField.GUID.getFieldId()));

		// Verify that name values are not truncated for this file type
		Assert.assertEquals(response.getGermplasmPreferredName(), dataRow.get(TermId.PREFERRED_NAME.getId()));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields_WhenXLSFileType() {
		this.labelPrinting.initStaticFields();
		final Set<Integer> keys = new HashSet<>(Arrays.asList(TermId.GID.getId(), LabelPrintingStaticField.GUID.getFieldId(),
			TermId.PREFERRED_NAME.getId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setGermplasmPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.XLS);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, null, response, new HashMap<>(), new HashMap<>(), new HashMap<>());
		Assert.assertEquals(3, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(TermId.GID.getId()));
		Assert.assertEquals(response.getGermplasmUUID(), dataRow.get(LabelPrintingStaticField.GUID.getFieldId()));

		// Verify that name values are not truncated for this file type
		Assert.assertEquals(response.getGermplasmPreferredName(), dataRow.get(TermId.PREFERRED_NAME.getId()));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields_WhenPDFFileType() {
		this.labelPrinting.initStaticFields();
		final Set<Integer> keys = new HashSet<>(Arrays.asList(TermId.GID.getId(), LabelPrintingStaticField.GUID.getFieldId(),
			TermId.PREFERRED_NAME.getId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setGermplasmPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.PDF);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, null, response, new HashMap<>(), new HashMap<>(), new HashMap<>());
		Assert.assertEquals(3, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(TermId.GID.getId()));
		Assert.assertEquals(response.getGermplasmUUID(), dataRow.get(LabelPrintingStaticField.GUID.getFieldId()));

		// Verify that name values are truncated for this file type
		Assert.assertEquals(response.getGermplasmPreferredName().substring(0, 200) + "...", dataRow.get(TermId.PREFERRED_NAME.getId()));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields_WhenCSVFileType() {
		this.labelPrinting.initStaticFields();
		final Set<Integer> keys =
			new HashSet<>(Arrays.asList(TermId.CROSS_FEMALE_GID.getId(), LabelPrintingStaticField.CROSS.getFieldId(),
				TermId.CROSS_FEMALE_PREFERRED_NAME.getId(), TermId.CROSS_MALE_PREFERRED_NAME.getId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setFemaleParentPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		response.setMaleParentPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.CSV);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), new HashMap<>(),
				new HashMap<>());
		Assert.assertEquals(4, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentGID(), dataRow.get(TermId.CROSS_FEMALE_GID.getId()));
		Assert.assertEquals(listData.getData().get(GermplasmListStaticColumns.CROSS.name()),
			dataRow.get(LabelPrintingStaticField.CROSS.getFieldId()));

		// Verify that name values are not truncated for this file type
		Assert.assertEquals(response.getFemaleParentPreferredName(), dataRow.get(TermId.CROSS_FEMALE_PREFERRED_NAME.getId()));
		Assert.assertEquals(response.getMaleParentPreferredName(), dataRow.get(TermId.CROSS_MALE_PREFERRED_NAME.getId()));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields_WhenXLSFileType() {
		this.labelPrinting.initStaticFields();
		final Set<Integer> keys =
			new HashSet<>(Arrays.asList(TermId.CROSS_FEMALE_GID.getId(), LabelPrintingStaticField.CROSS.getFieldId(),
				TermId.CROSS_FEMALE_PREFERRED_NAME.getId(), TermId.CROSS_MALE_PREFERRED_NAME.getId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setFemaleParentPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		response.setMaleParentPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.XLS);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), new HashMap<>(),
				new HashMap<>());
		Assert.assertEquals(4, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentGID(), dataRow.get(TermId.CROSS_FEMALE_GID.getId()));
		Assert.assertEquals(listData.getData().get(GermplasmListStaticColumns.CROSS.name()),
			dataRow.get(LabelPrintingStaticField.CROSS.getFieldId()));

		// Verify that name values are not truncated for this file type
		Assert.assertEquals(response.getFemaleParentPreferredName(), dataRow.get(TermId.CROSS_FEMALE_PREFERRED_NAME.getId()));
		Assert.assertEquals(response.getMaleParentPreferredName(), dataRow.get(TermId.CROSS_MALE_PREFERRED_NAME.getId()));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields_WhenPDFFileType() {
		this.labelPrinting.initStaticFields();
		final Set<Integer> keys =
			new HashSet<>(Arrays.asList(TermId.CROSS_FEMALE_GID.getId(), LabelPrintingStaticField.CROSS.getFieldId(),
				TermId.CROSS_FEMALE_PREFERRED_NAME.getId(), TermId.CROSS_MALE_PREFERRED_NAME.getId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setFemaleParentPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		response.setMaleParentPreferredName(RandomStringUtils.randomAlphanumeric(4000));
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.PDF);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), new HashMap<>(),
				new HashMap<>());
		Assert.assertEquals(4, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentGID(), dataRow.get(TermId.CROSS_FEMALE_GID.getId()));
		Assert.assertEquals(listData.getData().get(GermplasmListStaticColumns.CROSS.name()),
			dataRow.get(LabelPrintingStaticField.CROSS.getFieldId()));

		// Verify that name values are truncated for this file type
		Assert.assertEquals(response.getFemaleParentPreferredName().substring(0, 200) + "...",
			dataRow.get(TermId.CROSS_FEMALE_PREFERRED_NAME.getId()));
		Assert.assertEquals(response.getMaleParentPreferredName().substring(0, 200) + "...",
			dataRow.get(TermId.CROSS_MALE_PREFERRED_NAME.getId()));
	}

	@Test
	public void testGetDataRow_For_AttributeFields_WhenCSVFileType() {
		this.labelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(attributeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(GermplasmLabelPrinting.toId(attributeId), attributeValue);
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.CSV);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, attributeValues, new HashMap<>(),
				new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values are not truncated for CSV file type
		Assert.assertEquals(attributeValue, dataRow.get(attributeId));
	}

	@Test
	public void testGetDataRow_For_AttributeFields_WhenXLSFileType() {
		this.labelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(attributeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(GermplasmLabelPrinting.toId(attributeId), attributeValue);
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.XLS);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, attributeValues, new HashMap<>(),
				new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values are not truncated for XLS file type
		Assert.assertEquals(attributeValue, dataRow.get(attributeId));
	}

	@Test
	public void testGetDataRow_For_TruncateLongAttributeValues_WhenPDFFileType() {
		this.labelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(attributeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(GermplasmLabelPrinting.toId(attributeId), attributeValue);
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.PDF);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, attributeValues, new HashMap<>(),
				new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values are truncated for PDF file type
		Assert.assertEquals(attributeValue.substring(0, 200) + "...", dataRow.get(attributeId));
	}

	@Test
	public void testGetDataRow_For_NameFields_WhenCSVFileType() {
		this.labelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(nameTypeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(4000);
		nameValues.get(GID).put(GermplasmLabelPrinting.toId(nameTypeId), nameValue);
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.CSV);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), nameValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());

		// Verify that name values are truncated for this file type
		Assert.assertEquals(nameValue, dataRow.get(nameTypeId));
	}

	@Test
	public void testGetDataRow_For_NameFields_WhenXLSFileType() {
		this.labelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(nameTypeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(4000);
		nameValues.get(GID).put(GermplasmLabelPrinting.toId(nameTypeId), nameValue);
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.XLS);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), nameValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());

		// Verify that name values are not truncated for this file type
		Assert.assertEquals(nameValue, dataRow.get(nameTypeId));
	}

	@Test
	public void testGetDataRow_For_NameFields_WhenPDFFileType() {
		this.labelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(nameTypeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(4000);
		nameValues.get(GID).put(GermplasmLabelPrinting.toId(nameTypeId), nameValue);
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.PDF);
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), nameValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());

		// Verify that name values are truncated for this file type
		Assert.assertEquals(nameValue.substring(0, 200) + "...", dataRow.get(nameTypeId));
	}

	@Test
	public void testGetDataRow_For_DefaultEntryDetailFields() {
		this.labelPrinting.initStaticFields();
		final Set<Integer> keys = new HashSet<>(Arrays.asList(TermId.ENTRY_NO.getId(), TermId.ENTRY_CODE.getId()));
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), new HashMap<>(),
				new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(listData.getData().get(TermId.ENTRY_NO.name()), dataRow.get(TermId.ENTRY_NO.getId()));
		Assert.assertEquals(listData.getData().get(TermId.ENTRY_CODE.name()), dataRow.get(TermId.ENTRY_CODE.getId()));
	}

	@Test
	public void testGetDataRow_For_EntryDetailFields() {
		this.labelPrinting.initStaticFields();
		final Integer entryDetailVariableId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(entryDetailVariableId));
		final GermplasmListDataSearchResponse listData = this.createGermplasmListDataSearchResponse();
		final Map<Integer, Map<Integer, String>> entryDetailValues = new HashMap<>();
		entryDetailValues.put(listData.getListDataId(), new HashMap<>());
		final String entryDetailValue = RandomStringUtils.randomAlphanumeric(10);
		entryDetailValues.get(listData.getListDataId()).put(GermplasmLabelPrinting.toId(entryDetailVariableId), entryDetailValue);
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		final Map<Integer, String> dataRow =
			this.labelPrinting.getDataRow(labelsGeneratorInput, keys, listData, response, new HashMap<>(), new HashMap<>(),
				entryDetailValues);
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(entryDetailValue, dataRow.get(entryDetailVariableId));
	}

	private GermplasmSearchResponse createGermplasmSearchResponse() {
		final GermplasmSearchResponse response = new GermplasmSearchResponse();
		response.setGid(GID);
		response.setGermplasmUUID(RandomStringUtils.randomAlphanumeric(10));
		response.setFemaleParentGID(RandomStringUtils.randomNumeric(3));
		response.setPedigreeString(RandomStringUtils.randomAlphanumeric(10));
		return response;
	}

	private GermplasmListDataSearchResponse createGermplasmListDataSearchResponse() {
		final GermplasmListDataSearchResponse response = new GermplasmListDataSearchResponse();
		response.setListDataId(Integer.valueOf(RandomStringUtils.randomNumeric(3)));
		response.setData(new HashMap<>());
		response.getData().put(TermId.ENTRY_NO.name(), "1");
		response.getData().put(GermplasmListStaticColumns.CROSS.name(), RandomStringUtils.randomAlphanumeric(10));
		return response;
	}
}
