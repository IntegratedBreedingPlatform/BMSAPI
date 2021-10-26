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
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmListService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class GermplasmListLabelPrinting extends LabelPrintingStrategy {
	private List<Field> defaultPedigreeDetailsFields;
	private List<Field> defaultGermplasmDetailsFields;

	private List<Integer> pedigreeFieldIds;
	private List<Integer> germplasmFieldIds;

	protected List<SortableFieldDto> sortedByFields;


	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private GermplasmSearchService germplasmSearchService;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private GermplasmNameService germplasmNameService;

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private UserService userService;

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
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(labelsInfoInput.getListId()));

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

		final GermplasmListDto germplasmListDto = this.germplasmListService.getGermplasmListById(labelsInfoInput.getListId());
		final WorkbenchUser user = this.userService.getUserById(germplasmListDto.getOwnerId());
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(labelsInfoInput.getListId()));
		final long germplasmCount = this.germplasmService.countSearchGermplasm(germplasmSearchRequest, programUUID);
		final String tempFileName = "Labels-for-".concat(germplasmListDto.getListName());
		final String defaultFileName = FileNameGenerator.generateFileName(FileUtils.cleanFileName(tempFileName));

		final Map<String, String> resultsMap = new LinkedHashMap<>();
		resultsMap.put(this.getMessage("label.printing.list.name"), germplasmListDto.getListName());
		resultsMap.put(this.getMessage("label.printing.title"), germplasmListDto.getDescription());
		resultsMap.put(this.getMessage("label.printing.owner"), user.getPerson().getDisplayName());
		resultsMap.put(this.getMessage("label.printing.date"), Util.getSimpleDateFormat(Util.DATE_AS_NUMBER_FORMAT).format(germplasmListDto.getCreationDate()));
		resultsMap.put(this.getMessage("label.printing.noOfEntries"), String.valueOf(germplasmCount));
		return new OriginResourceMetadata(defaultFileName, resultsMap);
	}

	@Override
	public List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final List<LabelType> labelTypes = new LinkedList<>();
		final String germplasmPropValue = this.getMessage("label.printing.germplasm.details");
		final String namesPropValue = this.getMessage("label.printing.names.details");
		final String attributesPropValue = this.getMessage("label.printing.attributes.details");
		final String entryDetailsPropValue = this.getMessage("label.printing.entry.details");

		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(labelsInfoInput.getListId()));

		final List<GermplasmSearchResponse> germplasmSearchResponses = this.germplasmSearchService
			.searchGermplasm(germplasmSearchRequest, null, programUUID);

		// Germplasm Details labels
		final LabelType germplasmType = new LabelType(germplasmPropValue, germplasmPropValue);
		germplasmType.setFields(this.defaultGermplasmDetailsFields);
		germplasmType.getFields().addAll(this.defaultPedigreeDetailsFields);
		labelTypes.add(germplasmType);

		// Names labels
		final LabelType namesType = new LabelType(namesPropValue, namesPropValue);
		namesType.setFields(new ArrayList<>());
		labelTypes.add(namesType);

		// Attributes labels
		final LabelType attributesType = new LabelType(attributesPropValue, attributesPropValue);
		attributesType.setFields(new ArrayList<>());
		labelTypes.add(attributesType);

		// Entry Details labels
		final LabelType entryDetailsType = new LabelType(entryDetailsPropValue, entryDetailsPropValue);
		entryDetailsType.setFields(new ArrayList<>());
		labelTypes.add(entryDetailsType);

		if (!germplasmSearchResponses.isEmpty()) {
			final List<Integer> gids = germplasmSearchResponses.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
			final List<Variable> attributeVariables = this.germplasmAttributeService.getGermplasmAttributeVariables(gids, programUUID);
			final List<GermplasmNameTypeDTO> nameTypes = this.germplasmNameTypeService.getNameTypesByGIDList(gids);

			namesType.getFields().addAll(nameTypes.stream()
				.map(nameType -> new Field(toKey(nameType.getId()), nameType.getCode()))
				.collect(Collectors.toList()));

			attirbutesType.getFields().addAll(attributeVariables.stream()
				.map(attributeVariable -> new Field(toKey(attributeVariable.getId()),
					StringUtils.isNotBlank(attributeVariable.getAlias()) ? attributeVariable.getAlias() : attributeVariable.getName()))
				.collect(Collectors.toList()));
		}
		return labelTypes;
	}

	@Override
	public LabelsData getLabelsData(
		final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		// Get raw data
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(labelsGeneratorInput.getListId()));

		this.setAddedColumnsToSearchRequest(labelsGeneratorInput, germplasmSearchRequest);

		final List<Integer> listOfGermplasmDetailsAndPedrigreeIds = new ArrayList<>();
		listOfGermplasmDetailsAndPedrigreeIds.addAll(this.germplasmFieldIds);
		listOfGermplasmDetailsAndPedrigreeIds.addAll(this.pedigreeFieldIds);

		final List<Integer> selectedFieldIds =
			labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toList());

		final boolean fieldsContainsNamesOrAttributes =
			selectedFieldIds.stream().anyMatch(fieldId -> !listOfGermplasmDetailsAndPedrigreeIds.contains(fieldId));

		PageRequest pageRequest = null;
		if (!StringUtils.isBlank(labelsGeneratorInput.getSortBy())) {
			pageRequest = new PageRequest(0, this.maxTotalResults, new Sort(Sort.Direction.ASC, labelsGeneratorInput.getSortBy()));
		}

		final List<GermplasmSearchResponse> responseList =
			this.germplasmService.searchGermplasm(germplasmSearchRequest, pageRequest, programUUID);
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();

		if (fieldsContainsNamesOrAttributes) {
			final List<Integer> gids = responseList.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
			final Map<Integer, List<AttributeDTO>> attributesByGIDsMap = this.germplasmAttributeService.getAttributesByGIDsMap(gids);
			for (final Map.Entry<Integer, List<AttributeDTO>> gidAttributes : attributesByGIDsMap.entrySet()) {
				final Map<Integer, String> attributesMap = new HashMap<>();
				gidAttributes.getValue().forEach(attributeDTO -> attributesMap.put(attributeDTO.getAttributeDbId(), attributeDTO.getValue()));
				attributeValues.put(gidAttributes.getKey(), attributesMap);
			}
			final List<GermplasmNameDto> germplasmNames = this.germplasmNameService.getGermplasmNamesByGids(gids);
			germplasmNames.forEach(name -> {
				nameValues.putIfAbsent(name.getGid(), new HashMap<>());
				nameValues.get(name.getGid()).put(name.getNameTypeId(), name.getName());
			});
		}

		// Data to be exported
		final List<Map<Integer, String>> data = new ArrayList<>();
		final Set<Integer> keys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				keys.add(LabelPrintingStaticField.GUID.getFieldId());
			} else {
				keys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}

		for (final GermplasmSearchResponse germplasmSearchResponse : responseList) {
			data.add(this.getDataRow(keys, germplasmSearchResponse, attributeValues, nameValues));
		}

		return new LabelsData(LabelPrintingStaticField.GUID.getFieldId(), data);
	}

	private void setAddedColumnsToSearchRequest(final LabelsGeneratorInput labelsGeneratorInput,
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
				if (TermId.GID.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGid(), ""));
					continue;
				}

				if (LabelPrintingStaticField.GUID.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmUUID(), ""));
					continue;
				}

				if (TermId.GROUP_ID.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGroupId(), ""));
					continue;
				}

				if (TermId.GERMPLASM_LOCATION.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getLocationName(), ""));
					continue;
				}

				if (TermId.LOCATION_ABBR.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getLocationAbbr(), ""));
					continue;
				}

				if (TermId.BREEDING_METHOD.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getMethodName(), ""));
					continue;
				}

				if (TermId.PREFERRED_ID.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmPeferredId(), ""));
					continue;
				}

				if (TermId.PREFERRED_NAME.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmPeferredName(), ""));
					continue;
				}

				if (LabelPrintingStaticField.REFERENCE.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getReference(), ""));
					continue;
				}

				if (TermId.GERMPLASM_DATE.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmDate(), ""));
					continue;
				}

				if (LabelPrintingStaticField.METHOD_CODE.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getMethodCode(), ""));
					continue;
				}

				if (LabelPrintingStaticField.METHOD_NUMBER.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getMethodNumber(), ""));
					continue;
				}

				if (LabelPrintingStaticField.METHOD_GROUP.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getMethodGroup(), ""));
					continue;
				}

				if (LabelPrintingStaticField.GROUP_SOURCE_GID.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGroupSourceGID(), ""));
					continue;
				}

				if (LabelPrintingStaticField.GROUP_SOURCE_PREFERRED_NAME.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getGroupSourcePreferredName(), ""));
					continue;
				}

				if (TermId.AVAILABLE_INVENTORY.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getAvailableBalance(), ""));
					continue;
				}

				if (TermId.UNITS_INVENTORY.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getUnit(), ""));
					continue;
				}

				if (LabelPrintingStaticField.LOTS.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getLotCount(), ""));
				}

			} else if (this.pedigreeFieldIds.contains(id)) {
				if (LabelPrintingStaticField.CROSS.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getPedigreeString(), ""));
					continue;
				}

				if (TermId.CROSS_FEMALE_GID.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getFemaleParentGID(), ""));
					continue;
				}

				if (TermId.CROSS_MALE_GID.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getMaleParentGID(), ""));
					continue;
				}

				if (TermId.CROSS_MALE_PREFERRED_NAME.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getMaleParentPreferredName(), ""));
					continue;
				}

				if (TermId.CROSS_FEMALE_PREFERRED_NAME.getId() == id) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getFemaleParentPreferredName(), ""));
					continue;
				}

				if (LabelPrintingStaticField.INMEDIATE_SOURCE_GID.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getImmediateSourceGID(), ""));
					continue;
				}

				if (LabelPrintingStaticField.INMEDIATE_SOURCE_PREFERRED_NAME.getFieldId().equals(id)) {
					columns.put(key, Objects.toString(germplasmSearchResponse.getImmediateSourcePreferredName(), ""));
				}
			} else {
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
		}
		return columns;
	}

	private void addingColumnToGermplasmSearchRequest(final List<Integer> listOfSelectedFields,
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

	private static int toKey(final int id) {
		return id + MAX_FIXED_TYPE_INDEX;
	}

	private static int toId(final int key) {
		if (key > MAX_FIXED_TYPE_INDEX) {
			return key - MAX_FIXED_TYPE_INDEX;
		}
		return key;
	}

	public String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

}
