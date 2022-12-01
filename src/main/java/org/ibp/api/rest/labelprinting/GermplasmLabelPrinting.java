package org.ibp.api.rest.labelprinting;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.labelprinting.LabelPrintingPresetDTO;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.FieldType;
import org.ibp.api.rest.labelprinting.domain.LabelPrintingFieldUtils;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.domain.SortableFieldDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class GermplasmLabelPrinting extends LabelPrintingStrategy {

	public static final int ATTRIBUTE_DISPLAY_MAX_LENGTH = 150;
	public static final int NAME_DISPLAY_MAX_LENGTH = 150;

	List<Field> defaultPedigreeDetailsFields;
	List<Field> defaultGermplasmDetailsFields;

	List<Integer> pedigreeFieldIds;
	List<Integer> germplasmFieldIds;

	List<SortableFieldDto> sortedByFields;

	public static final String GERMPLASM_DATE = "GERMPLASM DATE";
	public static final String METHOD_ABBREV = "METHOD ABBREV";
	public static final String METHOD_NUMBER = "METHOD NUMBER";
	public static final String METHOD_GROUP = "METHOD GROUP";
	public static final String PREFERRED_NAME = "PREFERRED NAME";
	public static final String PREFERRED_ID = "PREFERRED ID";
	public static final String GROUP_SOURCE_GID = "GROUP SOURCE GID";
	public static final String GROUP_SOURCE = "GROUP SOURCE";
	public static final String IMMEDIATE_SOURCE_GID = "IMMEDIATE SOURCE GID";
	public static final String IMMEDIATE_SOURCE = "IMMEDIATE SOURCE";

	public static final String GID = "GID";
	public static final String GROUP_ID = "GROUP ID";
	public static final String MALE_PARENT_GID = "MGID";
	public static final String FEMALE_PARENT_GID = "FGID";
	public static final String CROSS_MALE_PREFERRED_NAME = "CROSS-MALE PREFERRED NAME";
	public static final String CROSS_FEMALE_PREFERRED_NAME = "CROSS-FEMALE PREFERRED NAME";
	public static final String ORIG_FINAL_NAME = "germplasm-labels";

	@Autowired
	ResourceBundleMessageSource messageSource;

	@Autowired
	GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	GermplasmSearchService germplasmSearchService;

	@Autowired
	GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	GermplasmService germplasmService;

	@Autowired
	GermplasmNameService germplasmNameService;

	@Value("${export.germplasm.max.total.results}")
	public int maxTotalResults;

	protected static final List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	@PostConstruct
	public void initStaticFields() {
		final String gidPropValue = this.getMessage("label.printing.field.germplasm.gid");
		final String preferredNamePropValue = this.getMessage("label.printing.field.germplasm.preferred.name");
		final String groupIdPropValue = this.getMessage("label.printing.field.germplasm.group.id");
		final String creationDatePropValue = this.getMessage("label.printing.field.germplasm.creation.date");

		this.sortedByFields = Arrays.asList(
			new SortableFieldDto(gidPropValue, GermplasmLabelPrinting.GID),
			new SortableFieldDto(preferredNamePropValue, GermplasmLabelPrinting.PREFERRED_NAME),
			new SortableFieldDto(groupIdPropValue, GermplasmLabelPrinting.GROUP_ID),
			new SortableFieldDto(creationDatePropValue, GermplasmLabelPrinting.GERMPLASM_DATE));

		this.defaultGermplasmDetailsFields = this.buildGermplasmDetailsFields();
		this.germplasmFieldIds = this.defaultGermplasmDetailsFields.stream().map(Field::getId).collect(Collectors.toList());

		this.defaultPedigreeDetailsFields = this.buildPedigreeDetailsFields();
		this.pedigreeFieldIds = this.defaultPedigreeDetailsFields.stream().map(Field::getId).collect(Collectors.toList());

	}

	@Override
	public void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
			.getSearchRequest(labelsInfoInput.getSearchRequestId(), GermplasmSearchRequest.class);

		final long germplasmCount = this.germplasmService.countSearchGermplasm(germplasmSearchRequest, programUUID);
		if (germplasmCount > this.maxTotalResults) {
			throw new ApiRequestValidationException(Arrays.asList(
				new ObjectError("", new String[] {"exceed.germplasm.export.labels.threshold"}, new Object[] {this.maxTotalResults}, null))
			);
		}
	}

	@Override
	public LabelsNeededSummary getSummaryOfLabelsNeeded(
		final LabelsInfoInput labelsInfoInput) {
		return null;
	}

	@Override
	public LabelsNeededSummaryResponse transformLabelsNeededSummary(
		final LabelsNeededSummary labelsNeededSummary) {
		return null;
	}

	@Override
	public OriginResourceMetadata getOriginResourceMetadata(
		final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final String fileName = FileNameGenerator.generateFileName(GermplasmLabelPrinting.ORIG_FINAL_NAME);
		return new OriginResourceMetadata(FileUtils.cleanFileName(fileName), new HashMap<>());
	}

	@Override
	public List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final List<LabelType> labelTypes = new LinkedList<>();
		final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
			.getSearchRequest(labelsInfoInput.getSearchRequestId(), GermplasmSearchRequest.class);
		final List<GermplasmSearchResponse> germplasmSearchResponses =
			this.germplasmSearchService.searchGermplasm(germplasmSearchRequest, null, programUUID);

		// Germplasm Details labels
		final String germplasmPropValue = this.getMessage("label.printing.germplasm.details");
		final LabelType germplasmType = new LabelType(germplasmPropValue, germplasmPropValue);
		germplasmType.setFields(this.defaultGermplasmDetailsFields);
		labelTypes.add(germplasmType);

		// Pedigree labels
		final String pedigreePropValue = this.getMessage("label.printing.pedigree.details");
		final LabelType pedigreeType = new LabelType(pedigreePropValue, pedigreePropValue);
		pedigreeType.setFields(this.defaultPedigreeDetailsFields);
		labelTypes.add(pedigreeType);

		this.populateNamesAndAttributesLabelType(programUUID, labelTypes, germplasmSearchResponses);
		return labelTypes;
	}

	void populateNamesAndAttributesLabelType(
		final String programUUID, final List<LabelType> labelTypes, final List<GermplasmSearchResponse> germplasmSearchResponses) {
		// Names labels
		final String namesPropValue = this.getMessage("label.printing.names.details");
		final LabelType namesType = new LabelType(namesPropValue, namesPropValue);
		namesType.setFields(new ArrayList<>());
		labelTypes.add(namesType);

		if (!germplasmSearchResponses.isEmpty()) {
			final List<Integer> gids = germplasmSearchResponses.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
			final List<Variable> attributeVariables = this.germplasmAttributeService.getGermplasmAttributeVariables(gids, programUUID);
			final List<GermplasmNameTypeDTO> nameTypes = this.germplasmNameTypeService.getNameTypesByGIDList(gids);

			this.populateAttributesLabelType(programUUID, labelTypes, gids, attributeVariables);

			namesType.getFields().addAll(nameTypes.stream()
				.map(nameType -> new Field(FieldType.NAME, nameType.getId(), nameType.getCode()))
				.collect(Collectors.toList()));
		} else {
			this.populateAttributesLabelType(programUUID, labelTypes, Collections.emptyList(), Collections.emptyList());
		}
	}

	@Override
	public LabelsData getLabelsData(
		final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		// Get Germplasm data
		final Integer searchRequestId = labelsGeneratorInput.getSearchRequestId();
		final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
			.getSearchRequest(searchRequestId, GermplasmSearchRequest.class);
		this.setAddedColumnsToSearchRequest(labelsGeneratorInput, germplasmSearchRequest);
		PageRequest pageRequest = null;
		if (!StringUtils.isBlank(labelsGeneratorInput.getSortBy())) {
			pageRequest = new PageRequest(0, this.maxTotalResults, new Sort(Sort.Direction.ASC, labelsGeneratorInput.getSortBy()));
		}
		final List<GermplasmSearchResponse> responseList =
			this.germplasmService.searchGermplasm(germplasmSearchRequest, pageRequest, programUUID);

		//Get Germplasm names and attributes data
		final List<Integer> nonNameAndAttributeIds = new ArrayList<>();
		nonNameAndAttributeIds.addAll(this.germplasmFieldIds);
		nonNameAndAttributeIds.addAll(this.pedigreeFieldIds);
		final Set<String> combinedKeys = this.getSelectedFieldIds(labelsGeneratorInput);
		final boolean fieldsContainsNamesOrAttributes =
			combinedKeys.stream().anyMatch(fieldId -> !nonNameAndAttributeIds.contains(LabelPrintingFieldUtils.getFieldIdFromCombinedKey(fieldId)));
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		if (fieldsContainsNamesOrAttributes) {
			final List<Integer> gids = responseList.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
			this.getAttributeValuesMap(attributeValues, gids);
			this.getNameValuesMap(nameValues, gids);
		}

		final boolean isPdf = FileType.PDF.equals(labelsGeneratorInput.getFileType());

		// Data to be exported
		final List<Map<String, String>> data = new ArrayList<>();
		for (final GermplasmSearchResponse germplasmSearchResponse : responseList) {
			data.add(this.getDataRow(isPdf, combinedKeys, germplasmSearchResponse, attributeValues, nameValues));
		}
		return new LabelsData(LabelPrintingFieldUtils.transformToCombinedKey(FieldType.STATIC, LabelPrintingStaticField.GUID.getFieldId()), data);
	}

	void getNameValuesMap(final Map<Integer, Map<Integer, String>> nameValues, final List<Integer> gids) {
		final List<GermplasmNameDto> germplasmNames = this.germplasmNameService.getGermplasmNamesByGids(gids);
		germplasmNames.forEach(name -> {
			nameValues.putIfAbsent(name.getGid(), new HashMap<>());
			nameValues.get(name.getGid()).put(name.getNameTypeId(), name.getName());
		});
	}

	void getAttributeValuesMap(final Map<Integer, Map<Integer, String>> attributeValues, final List<Integer> gids) {
		final Map<Integer, List<AttributeDTO>> attributesByGIDsMap = this.germplasmAttributeService.getAttributesByGIDsMap(gids);
		for (final Map.Entry<Integer, List<AttributeDTO>> gidAttributes : attributesByGIDsMap.entrySet()) {
			final Map<Integer, String> attributesMap = new HashMap<>();
			gidAttributes.getValue().forEach(attributeDTO -> attributesMap.put(attributeDTO.getAttributeDbId(), attributeDTO.getValue()));
			attributeValues.put(gidAttributes.getKey(), attributesMap);
		}
	}

	Set<String> getSelectedFieldIds(final LabelsGeneratorInput labelsGeneratorInput) {
		final Set<String> combinedKeys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				combinedKeys.add(LabelPrintingFieldUtils.transformToCombinedKey(FieldType.STATIC,LabelPrintingStaticField.GUID.getFieldId()));
			} else {
				combinedKeys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}
		return combinedKeys;
	}

	void setAddedColumnsToSearchRequest(
		final LabelsGeneratorInput labelsGeneratorInput,
		final GermplasmSearchRequest germplasmSearchRequest) {
		final Set<String> addedColumnsPropertyIds = new HashSet<>();

		labelsGeneratorInput.getFields().forEach(listOfSelectedFields ->
			this.addingColumnToGermplasmSearchRequest(listOfSelectedFields, addedColumnsPropertyIds)
		);

		if (!StringUtils.isBlank(labelsGeneratorInput.getSortBy())) {
			addedColumnsPropertyIds.add(labelsGeneratorInput.getSortBy());
		}

		germplasmSearchRequest.setAddedColumnsPropertyIds(new ArrayList<>(addedColumnsPropertyIds));
	}

	Map<String, String> getDataRow(final boolean isPdf,
		final Set<String> combinedKeys, final GermplasmSearchResponse germplasmSearchResponse,
		final Map<Integer, Map<Integer, String>> attributeValues, final Map<Integer, Map<Integer, String>> nameValues) {

		final Map<String, String> columns = new HashMap<>();
		for (final String combinedKey : combinedKeys) {
			final FieldType fieldType = FieldType.find(LabelPrintingFieldUtils.getFieldTypeNameFromCombinedKey(combinedKey));
			if (FieldType.VARIABLE.equals(fieldType)) {
				this.getDataRowFromVariableFieldType(columns, isPdf, combinedKey, germplasmSearchResponse, attributeValues);
			} else if (FieldType.STATIC.equals(fieldType)) {
				this.getDataRowFromStaticFieldType(columns,isPdf, combinedKey, germplasmSearchResponse);
			} else if (FieldType.NAME.equals(fieldType)) {
				this.getDataRowFromNameFieldType(columns, isPdf, combinedKey, germplasmSearchResponse, nameValues);
			}
		}
		return columns;
	}
	void getDataRowFromVariableFieldType(final Map<String, String> columns, final boolean isPdf, final String combinedKey,
		final GermplasmSearchResponse germplasmSearchResponse, final Map<Integer, Map<Integer, String>> attributeValues){
		final TermId term = TermId.getById(LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey));

		switch (term) {
			case GID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getGid(), ""));
				break;
			case GROUP_ID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getGroupId(), ""));
				break;
			case GERMPLASM_LOCATION:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getLocationName(), ""));
				break;
			case LOCATION_ABBR:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getLocationAbbr(), ""));
				break;
			case BREEDING_METHOD:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getMethodName(), ""));
				break;
			case PREFERRED_ID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getGermplasmPreferredId(), ""));
				break;
			case PREFERRED_NAME:
				columns.put(combinedKey, Objects.toString(
					this.truncateValueIfPdf(isPdf, germplasmSearchResponse.getGermplasmPreferredName(), NAME_DISPLAY_MAX_LENGTH),
					""));
				break;
			case GERMPLASM_DATE:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getGermplasmDate(), ""));
				break;
			case AVAILABLE_INVENTORY:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getAvailableBalance(), ""));
				break;
			case UNITS_INVENTORY:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getUnit(), ""));
				break;
			case CROSS_FEMALE_GID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getFemaleParentGID(), ""));
				break;
			case CROSS_MALE_GID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getMaleParentGID(), ""));
				break;
			case CROSS_MALE_PREFERRED_NAME:
				columns.put(combinedKey, Objects.toString(
					this.truncateValueIfPdf(isPdf, germplasmSearchResponse.getMaleParentPreferredName(), NAME_DISPLAY_MAX_LENGTH), ""));
				break;
			case CROSS_FEMALE_PREFERRED_NAME:
				columns.put(combinedKey, Objects.toString(
					this.truncateValueIfPdf(isPdf, germplasmSearchResponse.getFemaleParentPreferredName(), NAME_DISPLAY_MAX_LENGTH), ""));
				break;
			default:
				final Map<Integer, String> attributesByType = attributeValues.get(germplasmSearchResponse.getGid());
				if (attributesByType != null) {
					final String attributeValue = attributesByType.get(LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey));
					if (attributeValue != null) {
						// Truncate attribute values to 200 characters if export file type is PDF
						columns.put(combinedKey, this.truncateValueIfPdf(isPdf, attributeValue, GermplasmLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH));
					}
				}
		}
	}

	void getDataRowFromStaticFieldType(final Map<String, String> columns, final boolean isPdf, final String combinedKey,
		final GermplasmSearchResponse germplasmSearchResponse){
		final Optional<LabelPrintingStaticField> staticField = LabelPrintingStaticField.getByFieldId(LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey));
		switch (staticField.get()) {
			case GUID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getGermplasmUUID(), ""));
				break;
			case REFERENCE:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getReference(), ""));
				break;
			case METHOD_CODE:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getMethodCode(), ""));
				break;
			case METHOD_NUMBER:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getMethodNumber(), ""));
				break;
			case METHOD_GROUP:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getMethodGroup(), ""));
				break;
			case GROUP_SOURCE_GID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getGroupSourceGID(), ""));
				break;
			case GROUP_SOURCE_PREFERRED_NAME:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getGroupSourcePreferredName(), ""));
				break;
			case LOTS:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getLotCount(), ""));
				break;
			case CROSS:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getPedigreeString(), ""));
				break;
			case IMMEDIATE_SOURCE_GID:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getImmediateSourceGID(), ""));
				break;
			case IMMEDIATE_SOURCE_NAME:
				columns.put(combinedKey, Objects.toString(germplasmSearchResponse.getImmediateSourceName(), ""));
				break;
			default:
				//do nothing
		}
	}

	void getDataRowFromNameFieldType(final Map<String, String> columns, final boolean isPdf, final String combinedKey,
		final GermplasmSearchResponse germplasmSearchResponse, final Map<Integer, Map<Integer, String>> nameValues){
		final Map<Integer, String> namesByType = nameValues.get(germplasmSearchResponse.getGid());
		if (namesByType != null) {
			final String nameValue = namesByType.get(LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey));
			if (nameValue != null) {
				columns.put(combinedKey, this.truncateValueIfPdf(isPdf, nameValue, GermplasmLabelPrinting.NAME_DISPLAY_MAX_LENGTH));
			}
		}
	}

	private String truncateValueIfPdf(final boolean isPdf, final String value, final int maxLength) {
		return isPdf && StringUtils.length(value) > maxLength ?
			value.substring(0, maxLength) + "..." : value;
	}

	void addingColumnToGermplasmSearchRequest(
		final List<String> listOfSelectedFields,
		final Set<String> addedColumnsPropertyIds) {
		if (listOfSelectedFields.contains(TermId.GERMPLASM_DATE.getId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.GERMPLASM_DATE);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.METHOD_CODE.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.METHOD_ABBREV);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.METHOD_NUMBER.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.METHOD_NUMBER);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.METHOD_GROUP.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.METHOD_GROUP);
		}

		if (listOfSelectedFields.contains(TermId.PREFERRED_NAME.getId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.PREFERRED_NAME);
		}

		if (listOfSelectedFields.contains(TermId.PREFERRED_ID.getId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.PREFERRED_ID);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.GROUP_SOURCE_GID.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.GROUP_SOURCE_GID);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.GROUP_SOURCE_PREFERRED_NAME.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.GROUP_SOURCE);
		}

		if (listOfSelectedFields.contains(TermId.CROSS_MALE_GID.getId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.MALE_PARENT_GID);
		}

		if (listOfSelectedFields.contains(TermId.CROSS_FEMALE_GID.getId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.FEMALE_PARENT_GID);
		}

		if (listOfSelectedFields.contains(TermId.CROSS_MALE_PREFERRED_NAME.getId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.CROSS_MALE_PREFERRED_NAME);
		}

		if (listOfSelectedFields.contains(TermId.CROSS_FEMALE_PREFERRED_NAME.getId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.CROSS_FEMALE_PREFERRED_NAME);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.IMMEDIATE_SOURCE_GID.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.IMMEDIATE_SOURCE_GID);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.IMMEDIATE_SOURCE_NAME.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.IMMEDIATE_SOURCE);
		}
	}

	public List<Field> buildPedigreeDetailsFields() {
		final String crossPropValue = this.getMessage("label.printing.field.pedigree.cross");
		final String femaleParentGIDPropValue = this.getMessage("label.printing.field.pedigree.female.parent.gid");
		final String maleParentGIDPropValue = this.getMessage("label.printing.field.pedigree.male.parent.gid");
		final String maleParentPreferredNamePropValue = this.getMessage("label.printing.field.pedigree.male.parent.preferred.name");
		final String femaleParentPreferredNamePropValue = this.getMessage("label.printing.field.pedigree.female.parent.preferred.name");
		final String immediateSourceGIDPropValue = this.getMessage("label.printing.field.pedigree.immediate.souce.gid");
		final String immediateSourceNamePropValue = this.getMessage("label.printing.field.pedigree.immediate.source.name");

		return ImmutableList.<Field>builder()
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.CROSS.getFieldId(), crossPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.CROSS_FEMALE_GID.getId(), femaleParentGIDPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.CROSS_MALE_GID.getId(), maleParentGIDPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.CROSS_MALE_PREFERRED_NAME.getId(), maleParentPreferredNamePropValue))
			.add(new Field(FieldType.VARIABLE, TermId.CROSS_FEMALE_PREFERRED_NAME.getId(), femaleParentPreferredNamePropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.IMMEDIATE_SOURCE_GID.getFieldId(), immediateSourceGIDPropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.IMMEDIATE_SOURCE_NAME.getFieldId(), immediateSourceNamePropValue))
			.build();
	}

	public List<Field> buildGermplasmDetailsFields() {
		final String gidPropValue = this.getMessage("label.printing.field.germplasm.gid");
		final String guidPropValue = this.getMessage("label.printing.field.germplasm.guid");
		final String groupIdPropValue = this.getMessage("label.printing.field.germplasm.group.id");
		final String locationPropValue = this.getMessage("label.printing.field.germplasm.location");
		final String locationAbbrPropValue = this.getMessage("label.printing.field.germplasm.location.abbr");
		final String breedingMethodNamePropValue = this.getMessage("label.printing.field.germplasm.breeding.method.name");
		final String preferredIdPropValue = this.getMessage("label.printing.field.germplasm.preferred.id");
		final String preferredNamePropValue = this.getMessage("label.printing.field.germplasm.preferred.name");
		final String referencePropValue = this.getMessage("label.printing.field.germplasm.reference");
		final String creationDatePropValue = this.getMessage("label.printing.field.germplasm.creation.date");

		final String methodCodePropValue = this.getMessage("label.printing.field.germplasm.method.code");
		final String methodNumberPropValue = this.getMessage("label.printing.field.germplasm.method.number");
		final String methodGroupPropValue = this.getMessage("label.printing.field.germplasm.method.group");
		final String groupSourceGidPropValue = this.getMessage("label.printing.field.germplasm.group.source.gid");
		final String groupSourcePreferredNamePropValue = this.getMessage("label.printing.field.germplasm.group.source.preferred.name");
		final String availablePropValue = this.getMessage("label.printing.field.germplasm.available");
		final String unitsPropValue = this.getMessage("label.printing.field.germplasm.units");
		final String lotsPropValue = this.getMessage("label.printing.field.germplasm.lots");

		return ImmutableList.<Field>builder()
			.add(new Field(FieldType.VARIABLE, TermId.GID.getId(), gidPropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.GUID.getFieldId(), guidPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.GROUP_ID.getId(), groupIdPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.GERMPLASM_LOCATION.getId(), locationPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.LOCATION_ABBR.getId(), locationAbbrPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.BREEDING_METHOD.getId(), breedingMethodNamePropValue))
			.add(new Field(FieldType.VARIABLE, TermId.PREFERRED_ID.getId(), preferredIdPropValue))
			.add(new Field(FieldType.VARIABLE, TermId.PREFERRED_NAME.getId(), preferredNamePropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.REFERENCE.getFieldId(), referencePropValue))
			.add(new Field(FieldType.VARIABLE, TermId.GERMPLASM_DATE.getId(), creationDatePropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.METHOD_CODE.getFieldId(), methodCodePropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.METHOD_NUMBER.getFieldId(), methodNumberPropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.METHOD_GROUP.getFieldId(), methodGroupPropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.GROUP_SOURCE_GID.getFieldId(), groupSourceGidPropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.GROUP_SOURCE_PREFERRED_NAME.getFieldId(), groupSourcePreferredNamePropValue))
			.add(new Field(FieldType.VARIABLE, TermId.AVAILABLE_INVENTORY.getId(), availablePropValue))
			.add(new Field(FieldType.VARIABLE, TermId.UNITS_INVENTORY.getId(), unitsPropValue))
			.add(new Field(FieldType.STATIC, LabelPrintingStaticField.LOTS.getFieldId(), lotsPropValue))
			.build();
	}

	@Override
	public List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	@Override
	public List<SortableFieldDto> getSortableFields() {
		return this.sortedByFields;
	}

	@Override
	public LabelPrintingPresetDTO getDefaultSetting(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		return null;
	}

	public String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	List<Field> getDefaultPedigreeDetailsFields() {
		return this.defaultPedigreeDetailsFields;
	}

	List<Field> getDefaultGermplasmDetailsFields() {
		return this.defaultGermplasmDetailsFields;
	}

}
