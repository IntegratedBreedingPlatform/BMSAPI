package org.ibp.api.rest.labelprinting;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.ibp.api.domain.common.LabelPrintingStaticField;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

	public static final int MAX_GID_LIST_SIZE = 5000;

	private static List<Field> DEFAULT_PEDIGREE_DETAILS_FIELDS;
	private static List<Field> DEFAULT_GERMPLASM_DETAILS_FIELDS;

	private static List<Integer> PEDIGREE_FIELD_IDS;
	private static List<Integer> GERMPLASM_FIELD_IDS;

	public static List<SortableFieldDto> SORTED_BY;

	public final static String GERMPLASM_DATE = "GERMPLASM DATE";
	public final static String METHOD_ABBREV = "METHOD ABBREV";
	public final static String METHOD_NUMBER = "METHOD NUMBER";
	public final static String METHOD_GROUP = "METHOD GROUP";
	public final static String PREFERRED_NAME = "PREFERRED NAME";
	public final static String PREFERRED_ID = "PREFERRED ID";
	public final static String GROUP_SOURCE_GID = "GROUP SOURCE GID";
	public final static String GROUP_SOURCE = "GROUP SOURCE";
	public final static String IMMEDIATE_SOURCE_GID = "IMMEDIATE SOURCE GID";
	public final static String IMMEDIATE_SOURCE = "IMMEDIATE SOURCE";

	public final static String GID = "GID";
	public final static String GROUP_ID = "GROUP ID";

	public final static String MALE_PARENT_GID = "MGID";
	public final static String FEMALE_PARENT_GID = "FGID";
	public final static String CROSS_MALE_PREFERRED_NAME = "CROSS-MALE PREFERRED NAME";
	public final static String CROSS_FEMALE_PREFERRED_NAME = "CROSS-FEMALE PREFERRED NAME";

	public final static String ORIG_FINAL_NAME = "germplasm-labels";

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private GermplasmSearchService germplasmSearchService;

	@Autowired
	private GermplasmService germplasmService;

	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	@PostConstruct
	void initStaticFields() {
		final String gidPropValue = this.getMessage("label.printing.field.germplasm.gid");
		final String preferredNamePropValue = this.getMessage("label.printing.field.germplasm.preferred.name");
		final String groupIdPropValue = this.getMessage("label.printing.field.germplasm.group.id");
		final String creationDatePropValue = this.getMessage("label.printing.field.germplasm.creation.date");

		SORTED_BY = Arrays.asList(
			new SortableFieldDto(gidPropValue, GermplasmLabelPrinting.GID),
			new SortableFieldDto(preferredNamePropValue, GermplasmLabelPrinting.PREFERRED_NAME),
			new SortableFieldDto(groupIdPropValue, GermplasmLabelPrinting.GROUP_ID),
			new SortableFieldDto(creationDatePropValue, GermplasmLabelPrinting.GERMPLASM_DATE));

		DEFAULT_GERMPLASM_DETAILS_FIELDS = this.buildGermplasmDetailsFields();
		GERMPLASM_FIELD_IDS = DEFAULT_GERMPLASM_DETAILS_FIELDS.stream().map(field -> field.getId()).collect(Collectors.toList());

		DEFAULT_PEDIGREE_DETAILS_FIELDS = this.buildPedigreeDetailsFields();
		PEDIGREE_FIELD_IDS = DEFAULT_PEDIGREE_DETAILS_FIELDS.stream().map(field -> field.getId()).collect(Collectors.toList());

	}

	@Override
	public void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput) {

	}

	@Override
	LabelsNeededSummary getSummaryOfLabelsNeeded(
		final LabelsInfoInput labelsInfoInput) {
		return null;
	}

	@Override
	LabelsNeededSummaryResponse transformLabelsNeededSummary(
		final LabelsNeededSummary labelsNeededSummary) {
		return null;
	}

	@Override
	OriginResourceMetadata getOriginResourceMetadata(
		final LabelsInfoInput labelsInfoInput) {
		final String fileName = FileNameGenerator.generateFileName(GermplasmLabelPrinting.ORIG_FINAL_NAME);
		return new OriginResourceMetadata(FileUtils.cleanFileName(fileName), new HashMap<>());
	}

	@Override
	List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput) {
		final List<LabelType> labelTypes = new LinkedList<>();
		final String germplasmPropValue = this.getMessage("label.printing.germplasm.details");
		final String pedigreePropValue = this.getMessage("label.printing.pedigree.details");
		final String namesPropValue = this.getMessage("label.printing.names.details");
		final String attributesPropValue = this.getMessage("label.printing.attributes.details");

		final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
			.getSearchRequest(labelsInfoInput.getSearchRequestId(), GermplasmSearchRequest.class);

		final List<UserDefinedField> attributeTypes = this.germplasmSearchService.getGermplasmAttributeTypes(germplasmSearchRequest);
		final List<UserDefinedField> nameTypes = this.germplasmSearchService.getGermplasmNameTypes(germplasmSearchRequest);

		// Germplasm Details labels
		final LabelType germplasmType = new LabelType(germplasmPropValue, germplasmPropValue);
		germplasmType.setFields(DEFAULT_GERMPLASM_DETAILS_FIELDS);
		labelTypes.add(germplasmType);

		// Pedigree labels
		final LabelType pedigreeType = new LabelType(pedigreePropValue, pedigreePropValue);
		pedigreeType.setFields(DEFAULT_PEDIGREE_DETAILS_FIELDS);
		labelTypes.add(pedigreeType);

		// Names labels
		final LabelType namesType = new LabelType(namesPropValue, namesPropValue);
		namesType.setFields(new ArrayList<>());
		namesType.getFields().addAll(nameTypes.stream()
			.map(attr -> new Field(toKey(attr.getFldno()), attr.getFcode()))
			.collect(Collectors.toList()));
		labelTypes.add(namesType);

		// Attribiutes labels
		final LabelType attirbutesType = new LabelType(attributesPropValue, attributesPropValue);
		attirbutesType.setFields(new ArrayList<>());
		attirbutesType.getFields().addAll(attributeTypes.stream()
			.map(attr -> new Field(toKey(attr.getFldno()), attr.getFcode()))
			.collect(Collectors.toList()));
		labelTypes.add(attirbutesType);

		return labelTypes;
	}

	@Override
	LabelsData getLabelsData(
		final LabelsGeneratorInput labelsGeneratorInput) {
		// Get raw data
		final Integer searchRequestId = labelsGeneratorInput.getSearchRequestId();
		final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
			.getSearchRequest(searchRequestId, GermplasmSearchRequest.class);

		final Set<String> addedColumnsPropertyIds = new HashSet<>();

		labelsGeneratorInput.getFields().forEach((listOfSelectedFields) ->
			this.addingColumnToGermplasmSearchRequest(listOfSelectedFields, addedColumnsPropertyIds)
		);

		if (!StringUtils.isBlank(labelsGeneratorInput.getSortBy()) && !addedColumnsPropertyIds.contains(labelsGeneratorInput.getSortBy())) {
			addedColumnsPropertyIds.add(labelsGeneratorInput.getSortBy());
		}

		germplasmSearchRequest.setAddedColumnsPropertyIds(addedColumnsPropertyIds.stream().collect(Collectors.toList()));

		final List<Integer> listOfGermplasmDetailsAndPedrigreeIds = new ArrayList<>();
		listOfGermplasmDetailsAndPedrigreeIds.addAll(GERMPLASM_FIELD_IDS);
		listOfGermplasmDetailsAndPedrigreeIds.addAll(PEDIGREE_FIELD_IDS);

		final List<Integer> selectedFieldIds =
			labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toList());

		final boolean fieldsContainsNamesOrAttributes =
			selectedFieldIds.stream().anyMatch((fieldId) -> !listOfGermplasmDetailsAndPedrigreeIds.contains(fieldId));

		PageRequest pageRequest = null;
		if (!StringUtils.isBlank(labelsGeneratorInput.getSortBy())) {
			pageRequest = new PageRequest(0, GermplasmLabelPrinting.MAX_GID_LIST_SIZE, new Sort(Sort.Direction.ASC, labelsGeneratorInput.getSortBy()));
		}

		final List<GermplasmSearchResponse> responseList =
			this.germplasmService.searchGermplasm(germplasmSearchRequest, pageRequest, null);

		Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();

		if (fieldsContainsNamesOrAttributes) {
			attributeValues = this.germplasmSearchService.getGermplasmAttributeValues(germplasmSearchRequest);
			nameValues = this.germplasmSearchService.getGermplasmNameValues(germplasmSearchRequest);
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

	private Map<Integer, String> getDataRow(final Set<Integer> keys, final GermplasmSearchResponse germplasmSearchResponse,
		final Map<Integer, Map<Integer, String>> attributeValues, final Map<Integer, Map<Integer, String>> nameValues) {

		final Map<Integer, String> columns = new HashMap<>();
		for (final Integer key : keys) {
			final int id = toId(key);

			if (GERMPLASM_FIELD_IDS.contains(id)) {
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
					continue;
				}

			} else if (PEDIGREE_FIELD_IDS.contains(id)) {
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
					continue;
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
	List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	@Override
	List<SortableFieldDto> getSortableFields() {
		return SORTED_BY;
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

	String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

}
