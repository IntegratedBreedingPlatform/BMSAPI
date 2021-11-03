package org.ibp.api.rest.labelprinting;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.util.FileNameGenerator;
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
		Assert.assertEquals(
			FileNameGenerator.generateFileName(GermplasmLabelPrinting.ORIG_FINAL_NAME),
			originResourceMetadata.getDefaultFileName());
	}

	@Test
	public void testGetAvailableLabelTypes() {
		this.germplasmLabelPrinting.initStaticFields();
		Mockito.when(this.germplasmSearchService.searchGermplasm(this.germplasmSearchRequest, null, PROGRAM_UUID))
			.thenReturn(new ArrayList<>());

		final List<LabelType> labelTypes = this.germplasmLabelPrinting.getAvailableLabelTypes(this.labelsInfoInput, PROGRAM_UUID);

		Map<String, LabelType> labelTypeMap = labelTypes.stream().collect(Collectors.toMap(LabelType::getKey, Function.identity()));
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
		Map<String, LabelType> labelTypeMap = labelTypes.stream().collect(Collectors.toMap(LabelType::getKey, Function.identity()));
		Assert.assertEquals(1, labelTypeMap.get(NAMES).getFields().size());
		Assert
			.assertEquals(GermplasmLabelPrinting.toKey(nameTypeDTO.getId()), labelTypeMap.get(NAMES).getFields().get(0).getId().intValue());
		Assert.assertEquals(nameTypeDTO.getCode(), labelTypeMap.get(NAMES).getFields().get(0).getName());
		Assert.assertEquals(1, labelTypeMap.get(ATTRIBUTES).getFields().size());
		Assert.assertEquals(
			GermplasmLabelPrinting.toKey(variable.getId()),
			labelTypeMap.get(ATTRIBUTES).getFields().get(0).getId().intValue());
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
		Mockito.verify(this.germplasmAttributeService, Mockito.never()).getGermplasmAttributeVariables(ArgumentMatchers.anyList(), ArgumentMatchers.anyString());
		Mockito.verify(this.germplasmNameTypeService, Mockito.never()).getNameTypesByGIDList(ArgumentMatchers.anyList());
		Assert.assertEquals(LabelPrintingStaticField.GUID.getFieldId(), labelsData.getDefaultBarcodeKey());
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
		Mockito.when(this.germplasmNameService.getGermplasmNamesByGids(Collections.singletonList(GID))).thenReturn(Collections.singletonList(germplasmNameDto));
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();

		this.germplasmLabelPrinting.getNameValuesMap(nameValues, Collections.singletonList(GID));
		Assert.assertTrue(nameValues.containsKey(GID));
		Assert.assertEquals(germplasmNameDto.getName(), nameValues.get(GID).get(nameTypeId));
	}

	@Test
	public void testGetDataRow_For_GermplasmFields() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<Integer> keys = new HashSet<>(Arrays.asList(TermId.GID.getId(), LabelPrintingStaticField.GUID.getFieldId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, String> dataRow = this.germplasmLabelPrinting.getDataRow(keys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(String.valueOf(response.getGid()), dataRow.get(TermId.GID.getId()));
		Assert.assertEquals(response.getGermplasmUUID(), dataRow.get(LabelPrintingStaticField.GUID.getFieldId()));
	}

	@Test
	public void testGetDataRow_For_PedigreeFields() {
		this.germplasmLabelPrinting.initStaticFields();
		final Set<Integer> keys = new HashSet<>(Arrays.asList(TermId.CROSS_FEMALE_GID.getId(), LabelPrintingStaticField.CROSS.getFieldId()));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, String> dataRow = this.germplasmLabelPrinting.getDataRow(keys, response, new HashMap<>(), new HashMap<>());
		Assert.assertEquals(2, dataRow.keySet().size());
		Assert.assertEquals(response.getFemaleParentGID(), dataRow.get(TermId.CROSS_FEMALE_GID.getId()));
		Assert.assertEquals(response.getPedigreeString(), dataRow.get(LabelPrintingStaticField.CROSS.getFieldId()));
	}

	@Test
	public void testGetDataRow_For_AttributeFields() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(attributeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(10);
		attributeValues.get(GID).put(GermplasmLabelPrinting.toId(attributeId), attributeValue);
		final Map<Integer, String> dataRow = this.germplasmLabelPrinting.getDataRow(keys, response, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(attributeValue, dataRow.get(attributeId));
	}

	@Test
	public void testGetDataRow_For_NameFields() {
		this.germplasmLabelPrinting.initStaticFields();
		final Integer nameTypeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<Integer> keys = new HashSet<>(Collections.singletonList(nameTypeId));
		final GermplasmSearchResponse response = this.createGermplasmSearchResponse();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		nameValues.put(GID, new HashMap<>());
		final String nameValue = RandomStringUtils.randomAlphanumeric(10);
		nameValues.get(GID).put(GermplasmLabelPrinting.toId(nameTypeId), nameValue);
		final Map<Integer, String> dataRow = this.germplasmLabelPrinting.getDataRow(keys, response, new HashMap<>(), nameValues);
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(nameValue, dataRow.get(nameTypeId));
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
