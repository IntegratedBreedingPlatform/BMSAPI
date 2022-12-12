package org.ibp.api.rest.labelprinting;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.FieldType;
import org.ibp.api.rest.labelprinting.domain.LabelPrintingFieldUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmLabelPrintingTest {

	final static Integer SEARCH_REQUEST_ID = 1;
	final static String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);
	final static String GERMPLASM_DETAILS = "Germplasm Details";
	final static String PEDIGREE = "Pedigree";
	final static String NAMES = "Names";
	final static String ATTRIBUTES = "Attributes";
	final static Integer GID = 1;

	final static String GID_KEY = LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.GID.getId());

	final static String GUID_KEY = LabelPrintingFieldUtils.buildCombinedKey(
		FieldType.STATIC, LabelPrintingStaticField.GUID.getFieldId());

	final static String CROSS_FEMALE_GID_KEY = LabelPrintingFieldUtils.buildCombinedKey(
		FieldType.VARIABLE, TermId.CROSS_FEMALE_GID.getId());

	final static String PREFERRED_NAME_KEY = LabelPrintingFieldUtils.buildCombinedKey(
		FieldType.VARIABLE, TermId.PREFERRED_NAME.getId());

	final static String CROSS_KEY = LabelPrintingFieldUtils.buildCombinedKey(
		FieldType.STATIC, LabelPrintingStaticField.CROSS.getFieldId());

	final static String CROSS_MALE_PREFERRED_NAME_KEY = LabelPrintingFieldUtils.buildCombinedKey(
		FieldType.VARIABLE, TermId.CROSS_MALE_PREFERRED_NAME.getId());

	final static String CROSS_FEMALE_PREFERRED_NAME_KEY = LabelPrintingFieldUtils.buildCombinedKey(
		FieldType.VARIABLE, TermId.CROSS_FEMALE_PREFERRED_NAME.getId());

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private SearchRequestService searchRequestService;

	@Mock
	private GermplasmSearchService germplasmSearchService;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmAttributeService germplasmAttributeService;

	@Mock
	private GermplasmNameTypeService germplasmNameTypeService;

	@Mock
	private GermplasmNameService germplasmNameService;

	@InjectMocks
	private GermplasmLabelPrinting germplasmLabelPrinting;

	private GermplasmSearchRequest germplasmSearchRequest;
	private LabelsInfoInput labelsInfoInput;

	@Before
	public void setUp() {
		this.labelsInfoInput = new LabelsInfoInput();
		this.labelsInfoInput.setSearchRequestId(1);

		this.germplasmSearchRequest = new GermplasmSearchRequest();
		Mockito.when(this.searchRequestService
				.getSearchRequest(SEARCH_REQUEST_ID, GermplasmSearchRequest.class))
			.thenReturn(this.germplasmSearchRequest);

		Mockito.when(this.messageSource.getMessage("label.printing.germplasm.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(GERMPLASM_DETAILS);
		Mockito.when(this.messageSource.getMessage("label.printing.pedigree.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(PEDIGREE);
		Mockito.when(this.messageSource.getMessage("label.printing.names.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(NAMES);
		Mockito.when(this.messageSource.getMessage("label.printing.attributes.details", null, LocaleContextHolder.getLocale()))
			.thenReturn(ATTRIBUTES);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateLabelsInfoInputData() {
		Mockito.when(this.germplasmService.countSearchGermplasm(this.germplasmSearchRequest, PROGRAM_UUID)).thenReturn(3001L);
		this.germplasmLabelPrinting.validateLabelsInfoInputData(this.labelsInfoInput, PROGRAM_UUID);
	}

	@Test
	public void testGetOriginResourceMetadata() {
		final OriginResourceMetadata originResourceMetadata =
			this.germplasmLabelPrinting.getOriginResourceMetadata(this.labelsInfoInput, PROGRAM_UUID);
		Assert.assertTrue(MapUtils.isEmpty(originResourceMetadata.getMetadata()));
		Assert.assertTrue(originResourceMetadata.getDefaultFileName().startsWith(GermplasmLabelPrinting.ORIG_FINAL_NAME));
	}

	@Test
	public void testGetAvailableLabelTypes() {
		this.germplasmLabelPrinting.initStaticFields();
		Mockito.when(this.germplasmSearchService.searchGermplasm(this.germplasmSearchRequest, null, PROGRAM_UUID))
			.thenReturn(new ArrayList<>());

		final List<LabelType> labelTypes = this.germplasmLabelPrinting.getAvailableLabelTypes(this.labelsInfoInput, PROGRAM_UUID);

		final Map<String, LabelType> labelTypeMap = labelTypes.stream().collect(Collectors.toMap(LabelType::getKey, Function.identity()));
		Assert.assertEquals(4, labelTypes.size());
		Assert
			.assertEquals(this.germplasmLabelPrinting.getDefaultGermplasmDetailsFields(), labelTypeMap.get(GERMPLASM_DETAILS).getFields());
		Assert.assertEquals(this.germplasmLabelPrinting.getDefaultPedigreeDetailsFields(), labelTypeMap.get(PEDIGREE).getFields());
		Assert.assertTrue(CollectionUtils.isEmpty(labelTypeMap.get(NAMES).getFields()));
		Assert.assertTrue(CollectionUtils.isEmpty(labelTypeMap.get(ATTRIBUTES).getFields()));
	}

	@Test
	public void testPopulateNamesAndAttributesLabelType() {
		final List<LabelType> labelTypes = new ArrayList<>();
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();

		final Variable variable = new Variable();
		variable.setId(Integer.valueOf(RandomStringUtils.randomNumeric(5)));
		variable.setName(RandomStringUtils.randomAlphabetic(10));
		Mockito
			.when(this.germplasmAttributeService.getGermplasmAttributeVariables(Collections.singletonList(response.getGid()), PROGRAM_UUID))
			.thenReturn(Collections.singletonList(variable));

		final GermplasmNameTypeDTO nameTypeDTO = new GermplasmNameTypeDTO();
		nameTypeDTO.setId(Integer.valueOf(RandomStringUtils.randomNumeric(5)));
		nameTypeDTO.setCode(RandomStringUtils.randomAlphabetic(10));
		Mockito.when(this.germplasmNameTypeService.getNameTypesByGIDList(Collections.singletonList(response.getGid())))
			.thenReturn(Collections.singletonList(nameTypeDTO));

		this.germplasmLabelPrinting.populateNamesAndAttributesLabelType(PROGRAM_UUID, labelTypes, Collections.singletonList(response));
		Assert.assertEquals(2, labelTypes.size());
		final Map<String, LabelType> labelTypeMap = labelTypes.stream().collect(Collectors.toMap(LabelType::getKey, Function.identity()));
		Assert.assertEquals(1, labelTypeMap.get(NAMES).getFields().size());
		Assert.assertEquals(nameTypeDTO.getId(), labelTypeMap.get(NAMES).getFields().get(0).getId());
		Assert.assertEquals(nameTypeDTO.getCode(), labelTypeMap.get(NAMES).getFields().get(0).getName());
		Assert.assertEquals(1, labelTypeMap.get(ATTRIBUTES).getFields().size());
		Assert.assertEquals(variable.getId(), labelTypeMap.get(ATTRIBUTES).getFields().get(0).getId().intValue());
		Assert.assertEquals(variable.getName(), labelTypeMap.get(ATTRIBUTES).getFields().get(0).getName());
	}

	@Test
	public void testGetLabelsData() {
		this.germplasmLabelPrinting.initStaticFields();
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setSearchRequestId(SEARCH_REQUEST_ID);
		labelsGeneratorInput.setFields(new ArrayList<>());
		final LabelsData labelsData = this.germplasmLabelPrinting.getLabelsData(labelsGeneratorInput, PROGRAM_UUID);
		Mockito.verify(this.searchRequestService).getSearchRequest(SEARCH_REQUEST_ID, GermplasmSearchRequest.class);
		Mockito.verify(this.germplasmService).searchGermplasm(this.germplasmSearchRequest, null, PROGRAM_UUID);
		Mockito.verify(this.germplasmAttributeService, Mockito.never())
			.getGermplasmAttributeVariables(ArgumentMatchers.anyList(), ArgumentMatchers.anyString());
		Mockito.verify(this.germplasmNameTypeService, Mockito.never()).getNameTypesByGIDList(ArgumentMatchers.anyList());
		Assert.assertEquals(GermplasmLabelPrintingTest.GUID_KEY, labelsData.getDefaultBarcodeKey());
		Assert.assertTrue(CollectionUtils.isEmpty(labelsData.getData()));
	}

	@Test
	public void testGetAttributeValuesMap() {
		final Map<Integer, List<AttributeDTO>> attributesByGIDsMap = new HashMap<>();
		final Integer attributeDbId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setAttributeDbId(attributeDbId);
		attributeDTO.setValue(RandomStringUtils.randomAlphabetic(10));
		attributesByGIDsMap.put(1, Collections.singletonList(attributeDTO));
		Mockito.when(this.germplasmAttributeService.getAttributesByGIDsMap(Collections.singletonList(GID))).thenReturn(attributesByGIDsMap);
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();

		this.germplasmLabelPrinting.getAttributeValuesMap(attributeValues, Collections.singletonList(GID));
		Assert.assertTrue(attributeValues.containsKey(GID));
		Assert.assertEquals(attributesByGIDsMap.get(GID).get(0).getValue(), attributeValues.get(GID).get(attributeDbId));
	}

	@Test
	public void testGetNameValuesMap() {
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final GermplasmNameDto germplasmNameDto = new GermplasmNameDto();
		germplasmNameDto.setNameTypeId(nameTypeId);
		germplasmNameDto.setName(RandomStringUtils.randomAlphabetic(10));
		germplasmNameDto.setGid(GID);
		Mockito.when(this.germplasmNameService.getGermplasmNamesByGids(Collections.singletonList(GID)))
			.thenReturn(Collections.singletonList(germplasmNameDto));
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();

		this.germplasmLabelPrinting.getNameValuesMap(nameValues, Collections.singletonList(GID));
		Assert.assertTrue(nameValues.containsKey(GID));
		Assert.assertEquals(germplasmNameDto.getName(), nameValues.get(GID).get(nameTypeId));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys = new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.GID_KEY, GermplasmLabelPrintingTest.GUID_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(GermplasmLabelPrintingTest.GID_KEY));
		Assert.assertEquals(response.getGermplasmUUID(), dataRow.get(GermplasmLabelPrintingTest.GUID_KEY));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields_ShortNameValues() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.GID_KEY, GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setGermplasmPreferredName(RandomStringUtils.randomAlphabetic(50));
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(GermplasmLabelPrintingTest.GID_KEY));
		Assert.assertEquals(response.getGermplasmPreferredName(), dataRow.get(GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields_LongNameValuesTruncated_WhenPDFFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.GID_KEY, GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setGermplasmPreferredName(RandomStringUtils.randomAlphabetic(4000));
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(GermplasmLabelPrintingTest.GID_KEY));
		Assert.assertEquals(response.getGermplasmPreferredName().substring(0, GermplasmLabelPrinting.NAME_DISPLAY_MAX_LENGTH) + "...",
			dataRow.get(GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields_LongNameValuesNotTruncated_WhenCSVFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.GID_KEY, GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setGermplasmPreferredName(RandomStringUtils.randomAlphabetic(4000));
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(GermplasmLabelPrintingTest.GID_KEY));
		Assert.assertEquals(response.getGermplasmPreferredName(), dataRow.get(GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields_LongNameValuesNotTruncated_WhenXLSFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.GID_KEY, GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setGermplasmPreferredName(RandomStringUtils.randomAlphabetic(4000));
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(GermplasmLabelPrintingTest.GID_KEY));
		Assert.assertEquals(response.getGermplasmPreferredName(), dataRow.get(GermplasmLabelPrintingTest.PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.CROSS_FEMALE_GID_KEY, GermplasmLabelPrintingTest.CROSS_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentGID(), dataRow.get(GermplasmLabelPrintingTest.CROSS_FEMALE_GID_KEY));
		Assert.assertEquals(response.getPedigreeString(), dataRow.get(GermplasmLabelPrintingTest.CROSS_KEY));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields_ShortNameValues() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY,
				GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setFemaleParentPreferredName(RandomStringUtils.randomNumeric(100));
		response.setMaleParentPreferredName(RandomStringUtils.randomNumeric(100));
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentPreferredName(),
			dataRow.get(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY));
		Assert.assertEquals(response.getMaleParentPreferredName(), dataRow.get(GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields_LongNameValuesTruncated_WhenPDFFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY,
				GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setFemaleParentPreferredName(RandomStringUtils.randomNumeric(4000));
		response.setMaleParentPreferredName(RandomStringUtils.randomNumeric(4000));
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentPreferredName().substring(0, GermplasmLabelPrinting.NAME_DISPLAY_MAX_LENGTH) + "...",
			dataRow.get(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY));
		Assert.assertEquals(response.getMaleParentPreferredName().substring(0, GermplasmLabelPrinting.NAME_DISPLAY_MAX_LENGTH) + "...",
			dataRow.get(GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields_LongNameValuesNotTruncated_WhenCSVFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY,
				GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setFemaleParentPreferredName(RandomStringUtils.randomNumeric(4000));
		response.setMaleParentPreferredName(RandomStringUtils.randomNumeric(4000));
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentPreferredName(),
			dataRow.get(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY));
		Assert.assertEquals(response.getMaleParentPreferredName(), dataRow.get(GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields_LongNameValuesNotTruncated_WhenXLSFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<String> combinedKeys =
			new HashSet<>(Arrays.asList(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY,
				GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		response.setFemaleParentPreferredName(RandomStringUtils.randomNumeric(4000));
		response.setMaleParentPreferredName(RandomStringUtils.randomNumeric(4000));
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.XLS);
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(false, combinedKeys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentPreferredName(),
			dataRow.get(GermplasmLabelPrintingTest.CROSS_FEMALE_PREFERRED_NAME_KEY));
		Assert.assertEquals(response.getMaleParentPreferredName(), dataRow.get(GermplasmLabelPrintingTest.CROSS_MALE_PREFERRED_NAME_KEY));
	}

	@Test
	public void testGetDataRow_For_ShortAttributeValues() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(100);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(attributeValue, dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
	}

	@Test
	public void testGetDataRow_For_LongAttributeValuesNotTruncated_WhenCSVFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values is not truncated for CSV file type
		Assert.assertEquals(attributeValue, dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
	}

	@Test
	public void testGetDataRow_For_AttributeFields_WhenXLSFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values is not truncated for XLS file type
		Assert.assertEquals(attributeValue, dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
	}

	@Test
	public void testGetDataRow_For_TruncateLongAttributeValues_WhenPDFExportType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(attributeValue.substring(0, GermplasmLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH) + "...",
			dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, attributeId)));
	}

	@Test
	public void testGetDataRow_For_NameFields() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(10);
		nameValues.get(GID).put(nameTypeId, nameValue);
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), nameValues);
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(nameValue, dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
	}

	@Test
	public void testGetDataRow_For_ShortNameValues() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(10);
		nameValues.get(GID).put(nameTypeId, nameValue);
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), nameValues);
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(nameValue, dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
	}

	@Test
	public void testGetDataRow_For_LongNameValuesNotTruncated_WhenCSVFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(4000);
		nameValues.get(GID).put(nameTypeId, nameValue);
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), nameValues);
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(nameValue, dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
	}

	@Test
	public void testGetDataRow_For_LongNameValuesNotTruncated_WhenXLSFileType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(4000);
		nameValues.get(GID).put(nameTypeId, nameValue);
		final boolean isPdf = false;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), nameValues);
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(nameValue, dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
	}

	@Test
	public void testGetDataRow_For_TruncateLongNameValues_WhenPDFExportType() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> combinedKeys =
			new HashSet<>(Collections.singletonList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(4000);
		nameValues.get(GID).put(nameTypeId, nameValue);
		final boolean isPdf = true;
		final Map<String, String> dataRow =
			this.germplasmLabelPrinting.getDataRow(isPdf, combinedKeys, response, new HashMap<>(), nameValues);
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(nameValue.substring(0, GermplasmLabelPrinting.NAME_DISPLAY_MAX_LENGTH) + "...",
			dataRow.get(LabelPrintingFieldUtils.buildCombinedKey(FieldType.NAME, nameTypeId)));
	}

	private GermplasmSearchResponse createGermplasmSearchResponse() {
		final GermplasmSearchResponse response = new GermplasmSearchResponse();
		response.setGid(GID);
		response.setGermplasmUUID(RandomStringUtils.randomAlphanumeric(10));
		response.setFemaleParentGID(RandomStringUtils.randomNumeric(3));
		response.setPedigreeString(RandomStringUtils.randomAlphanumeric(10));
		return response;
	}
}
