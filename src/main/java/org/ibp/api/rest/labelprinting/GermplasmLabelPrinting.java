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
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class GermplasmLabelPrinting extends LabelPrintingStrategy {
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
				new ObjectError("", new String[] {"exceed.germplasm.export.labels.threshold"}, new Object[]{this.maxTotalResults}, null))
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
		final List<GermplasmSearchResponse> germplasmSearchResponses = this.germplasmSearchService.searchGermplasm(germplasmSearchRequest, null, programUUID);

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

		// Attributes labels
		final String attributesPropValue = this.getMessage("label.printing.attributes.details");
		final LabelType attributesType = new LabelType(attributesPropValue, attributesPropValue);
		attributesType.setFields(new ArrayList<>());
		labelTypes.add(attributesType);

		if (!germplasmSearchResponses.isEmpty()) {
			final List<Integer> gids = germplasmSearchResponses.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
			final List<Variable> attributeVariables = this.germplasmAttributeService.getGermplasmAttributeVariables(gids, programUUID);
			final List<GermplasmNameTypeDTO> nameTypes = this.germplasmNameTypeService.getNameTypesByGIDList(gids);

			namesType.getFields().addAll(nameTypes.stream()
				.map(nameType -> new Field(toKey(nameType.getId()), nameType.getCode()))
				.collect(Collectors.toList()));

			attributesType.getFields().addAll(attributeVariables.stream()
				.map(attributeVariable -> new Field(toKey(attributeVariable.getId()),
					StringUtils.isNotBlank(attributeVariable.getAlias()) ? attributeVariable.getAlias() : attributeVariable.getName()))
				.collect(Collectors.toList()));
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
		final Set<Integer> keys = this.getSelectedFieldIds(labelsGeneratorInput);
		final boolean fieldsContainsNamesOrAttributes =
			keys.stream().anyMatch(fieldId -> !nonNameAndAttributeIds.contains(fieldId));
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		if (fieldsContainsNamesOrAttributes) {
			final List<Integer> gids = responseList.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
			this.getAttributeValuesMap(attributeValues, gids);
			this.getNameValuesMap(nameValues, gids);
		}

		// Data to be exported
		final List<Map<Integer, String>> data = new ArrayList<>();
		for (final GermplasmSearchResponse germplasmSearchResponse : responseList) {
			data.add(this.getDataRow(keys, germplasmSearchResponse, attributeValues, nameValues));
		}

		return new LabelsData(LabelPrintingStaticField.GUID.getFieldId(), data);
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

	Set<Integer> getSelectedFieldIds(final LabelsGeneratorInput labelsGeneratorInput) {
		final Set<Integer> keys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				keys.add(LabelPrintingStaticField.GUID.getFieldId());
			} else {
				keys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}
		return keys;
	}

	void setAddedColumnsToSearchRequest(final LabelsGeneratorInput labelsGeneratorInput,
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

	private Map<Integer, String> getDataRow(final Set<Integer> keys, final GermplasmSearchResponse germplasmSearchResponse,
		final Map<Integer, Map<Integer, String>> attributeValues, final Map<Integer, Map<Integer, String>> nameValues) {

		final Map<Integer, String> columns = new HashMap<>();
		for (final Integer key : keys) {
			final int id = toId(key);
			if (this.germplasmFieldIds.contains(id)) {
				this.getGermplasmFieldDataRowValue(germplasmSearchResponse, columns, key, id);
			} else if (this.pedigreeFieldIds.contains(id)) {
				this.getPedigreeFieldDataRowValue(germplasmSearchResponse, columns, key, id);
			} else {
				this.getAttributeOrNameDataRowValue(germplasmSearchResponse, attributeValues, nameValues, columns, key, id);
			}
		}
		return columns;
	}

	void getAttributeOrNameDataRowValue(
		final GermplasmSearchResponse germplasmSearchResponse, final Map<Integer, Map<Integer, String>> attributeValues,
		final Map<Integer, Map<Integer, String>> nameValues, final Map<Integer, String> columns, final Integer key, final int id) {
		// Not part of the fixed columns
		// Attributes
		final Map<Integer, String> attributesByType = attributeValues.get(germplasmSearchResponse.getGid());
		if (attributesByType != null) {
			final String attributeValue = attributesByType.get(id);
			if (attributeValue != null) {
				columns.put(key, attributeValue);
			}
		}

		// Not part of the fixed columns
		// Name
		final Map<Integer, String> namesByType = nameValues.get(germplasmSearchResponse.getGid());
		if (namesByType != null) {
			final String nameValue = namesByType.get(id);
			if (nameValue != null) {
				columns.put(key, nameValue);
			}
		}
	}

	void getPedigreeFieldDataRowValue(
		final GermplasmSearchResponse germplasmSearchResponse, final Map<Integer, String> columns, final Integer key, final int id) {
		final TermId term = TermId.getById(id);
		switch (term) {
			case CROSS_FEMALE_GID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getFemaleParentGID(), ""));
				return;
			case CROSS_MALE_GID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getMaleParentGID(), ""));
				return;
			case CROSS_MALE_PREFERRED_NAME:
				columns.put(key, Objects.toString(germplasmSearchResponse.getMaleParentPreferredName(), ""));
				return;
			case CROSS_FEMALE_PREFERRED_NAME:
				columns.put(key, Objects.toString(germplasmSearchResponse.getFemaleParentPreferredName(), ""));
				return;
			default:
				//do nothing
		}
		final LabelPrintingStaticField staticField = LabelPrintingStaticField.getByFieldId(id);
		switch (staticField) {
			case CROSS:
				columns.put(key, Objects.toString(germplasmSearchResponse.getPedigreeString(), ""));
				return;
			case INMEDIATE_SOURCE_GID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getImmediateSourceGID(), ""));
				return;
			case INMEDIATE_SOURCE_PREFERRED_NAME:
				columns.put(key, Objects.toString(germplasmSearchResponse.getImmediateSourcePreferredName(), ""));
				return;
			default:
				//do nothing
		}
	}

	void getGermplasmFieldDataRowValue(
		final GermplasmSearchResponse germplasmSearchResponse, final Map<Integer, String> columns, final Integer key, final int id) {
		final TermId term = TermId.getById(id);
		switch (term) {
			case GID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGid(), ""));
				return;
			case GROUP_ID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGroupId(), ""));
				return;
			case GERMPLASM_LOCATION:
				columns.put(key, Objects.toString(germplasmSearchResponse.getLocationName(), ""));
				return;
			case LOCATION_ABBR:
				columns.put(key, Objects.toString(germplasmSearchResponse.getLocationAbbr(), ""));
				return;
			case BREEDING_METHOD:
				columns.put(key, Objects.toString(germplasmSearchResponse.getMethodName(), ""));
				return;
			case PREFERRED_ID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmPeferredId(), ""));
				return;
			case PREFERRED_NAME:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmPeferredName(), ""));
				return;
			case GERMPLASM_DATE:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmDate(), ""));
				return;
			case AVAILABLE_INVENTORY:
				columns.put(key, Objects.toString(germplasmSearchResponse.getAvailableBalance(), ""));
				return;
			case UNITS_INVENTORY:
				columns.put(key, Objects.toString(germplasmSearchResponse.getUnit(), ""));
				return;
			default:
				//do nothing
		}
		
		final LabelPrintingStaticField staticField = LabelPrintingStaticField.getByFieldId(id);
		switch (staticField) {
			case GUID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmUUID(), ""));
				return;
			case REFERENCE:
				columns.put(key, Objects.toString(germplasmSearchResponse.getReference(), ""));
				return;
			case METHOD_CODE:
				columns.put(key, Objects.toString(germplasmSearchResponse.getMethodCode(), ""));
				return;
			case METHOD_NUMBER:
				columns.put(key, Objects.toString(germplasmSearchResponse.getMethodNumber(), ""));
				return;
			case METHOD_GROUP:
				columns.put(key, Objects.toString(germplasmSearchResponse.getMethodGroup(), ""));
				return;
			case GROUP_SOURCE_GID:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGroupSourceGID(), ""));
				return;
			case GROUP_SOURCE_PREFERRED_NAME:
				columns.put(key, Objects.toString(germplasmSearchResponse.getGroupSourcePreferredName(), ""));
				return;
			case LOTS:
				columns.put(key, Objects.toString(germplasmSearchResponse.getLotCount(), ""));
				return;
			default:
				//do nothing
		}
	}

	void addingColumnToGermplasmSearchRequest(final List<Integer> listOfSelectedFields,
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

		if (listOfSelectedFields.contains(LabelPrintingStaticField.INMEDIATE_SOURCE_GID.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.IMMEDIATE_SOURCE_GID);
		}

		if (listOfSelectedFields.contains(LabelPrintingStaticField.INMEDIATE_SOURCE_PREFERRED_NAME.getFieldId())) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.IMMEDIATE_SOURCE);
		}
	}

	public List<Field> buildPedigreeDetailsFields() {
		final String crossPropValue = this.getMessage("label.printing.field.pedigree.cross");
		final String femaleParentGIDPropValue = this.getMessage("label.printing.field.pedigree.female.parent.gid");
		final String maleParentGIDPropValue = this.getMessage("label.printing.field.pedigree.male.parent.gid");
		final String maleParentPreferredNamePropValue = this.getMessage("label.printing.field.pedigree.male.parent.preferred.name");
		final String femaleParentPreferredNamePropValue = this.getMessage("label.printing.field.pedigree.female.parent.preferred.name");
		final String inmediateSourceGIDPropValue = this.getMessage("label.printing.field.pedigree.inmediate.souce.gid");
		final String inmediateSourcePreferredNamePropValue =
			this.getMessage("label.printing.field.pedigree.inmediate.source.preferred.name");

		return ImmutableList.<Field>builder()
			.add(new Field(LabelPrintingStaticField.CROSS.getFieldId(), crossPropValue))
			.add(new Field(TermId.CROSS_FEMALE_GID.getId(), femaleParentGIDPropValue))
			.add(new Field(TermId.CROSS_MALE_GID.getId(), maleParentGIDPropValue))
			.add(new Field(TermId.CROSS_MALE_PREFERRED_NAME.getId(), maleParentPreferredNamePropValue))
			.add(new Field(TermId.CROSS_FEMALE_PREFERRED_NAME.getId(), femaleParentPreferredNamePropValue))
			.add(new Field(LabelPrintingStaticField.INMEDIATE_SOURCE_GID.getFieldId(), inmediateSourceGIDPropValue))
			.add(
				new Field(LabelPrintingStaticField.INMEDIATE_SOURCE_PREFERRED_NAME.getFieldId(), inmediateSourcePreferredNamePropValue))
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
			.add(new Field(TermId.GID.getId(), gidPropValue))
			.add(new Field(LabelPrintingStaticField.GUID.getFieldId(), guidPropValue))
			.add(new Field(TermId.GROUP_ID.getId(), groupIdPropValue))
			.add(new Field(TermId.GERMPLASM_LOCATION.getId(), locationPropValue))
			.add(new Field(TermId.LOCATION_ABBR.getId(), locationAbbrPropValue))
			.add(new Field(TermId.BREEDING_METHOD.getId(), breedingMethodNamePropValue))
			.add(new Field(TermId.PREFERRED_ID.getId(), preferredIdPropValue))
			.add(new Field(TermId.PREFERRED_NAME.getId(), preferredNamePropValue))
			.add(new Field(LabelPrintingStaticField.REFERENCE.getFieldId(), referencePropValue))
			.add(new Field(TermId.GERMPLASM_DATE.getId(), creationDatePropValue))
			.add(new Field(LabelPrintingStaticField.METHOD_CODE.getFieldId(), methodCodePropValue))
			.add(new Field(LabelPrintingStaticField.METHOD_NUMBER.getFieldId(), methodNumberPropValue))
			.add(new Field(LabelPrintingStaticField.METHOD_GROUP.getFieldId(), methodGroupPropValue))
			.add(new Field(LabelPrintingStaticField.GROUP_SOURCE_GID.getFieldId(), groupSourceGidPropValue))
			.add(new Field(LabelPrintingStaticField.GROUP_SOURCE_PREFERRED_NAME.getFieldId(), groupSourcePreferredNamePropValue))
			.add(new Field(TermId.AVAILABLE_INVENTORY.getId(), availablePropValue))
			.add(new Field(TermId.UNITS_INVENTORY.getId(), unitsPropValue))
			.add(new Field(LabelPrintingStaticField.LOTS.getFieldId(), lotsPropValue))
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

	/**
	 * Identify non-fixed columns with id = MAX_FIXED_TYPE_INDEX + column-id
	 * Requires no collision between non-fixed columns id
	 * Allocates some space for future fixed-columns
	 */
	private static final Integer MAX_FIXED_TYPE_INDEX = 10000;

	static int toKey(final int id) {
		return id + MAX_FIXED_TYPE_INDEX;
	}

	static int toId(final int key) {
		if (key > MAX_FIXED_TYPE_INDEX) {
			return key - MAX_FIXED_TYPE_INDEX;
		}
		return key;
	}

	public String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

}
