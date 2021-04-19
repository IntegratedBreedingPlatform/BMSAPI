package org.ibp.api.rest.labelprinting;

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
import org.ibp.api.rest.labelprinting.domain.Sortable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Transactional
public class GermplasmLabelPrinting extends LabelPrintingStrategy {

	public static final int MAX_GID_LIST_SIZE = 5000;

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

	public static List<Sortable> SORTED_BY = Arrays.asList(new Sortable(GERMPLASM_DETAILS_FIELD.GID.name(), GermplasmLabelPrinting.GID),
		new Sortable(GERMPLASM_DETAILS_FIELD.PREFERRED_NAME.name(), GermplasmLabelPrinting.PREFERRED_NAME),
		new Sortable(GERMPLASM_DETAILS_FIELD.GROUP_ID.name(), GermplasmLabelPrinting.GROUP_ID),
		new Sortable(GERMPLASM_DETAILS_FIELD.CREATION_DATE.name(), GermplasmLabelPrinting.GERMPLASM_DATE));


	private enum GERMPLASM_DETAILS_FIELD {
		GID(TermId.GID.getId(), GermplasmLabelPrinting.GID),
		GUID(LabelPrintingStaticField.GUID.getFieldId(), "GUID"),
		GROUP_ID(TermId.GROUP_ID.getId(), "GroupID"),
		LOCATION(TermId.GERMPLASM_LOCATION.getId(), "Location"),
		LOCATION_ABBR(TermId.LOCATION_ABBR.getId(), "Location Abbr"),
		BREEDING_METHOD_NAME(TermId.BREEDING_METHOD.getId(), "Breeding Method Name"),
		PREFERRED_ID(TermId.PREFERRED_ID.getId(), "PreferredID"),
		PREFERRED_NAME(TermId.PREFERRED_NAME.getId(), "Preferred Name"),
		REFERENCE(LabelPrintingStaticField.REFERENCE.getFieldId(), "Reference"),
		CREATION_DATE(TermId.GERMPLASM_DATE.getId(), "Creation Date"),
		METHOD_CODE(LabelPrintingStaticField.METHOD_CODE.getFieldId(), "Method Code"),
		METHOD_NUMBER(LabelPrintingStaticField.METHOD_NUMBER.getFieldId(), "Method Number"),
		METHOD_GROUP(LabelPrintingStaticField.METHOD_GROUP.getFieldId(), "Method Group"),
		GROUP_SOURCE_GID(LabelPrintingStaticField.GROUP_SOURCE_GID.getFieldId(), "Group Source GID"),
		GROUP_SOURCE_PREFERRED_NAME(LabelPrintingStaticField.GROUP_SOURCE_PREFERRED_NAME.getFieldId(), "Group Source Preferred Name"),
		AVAILABLE(TermId.AVAILABLE_INVENTORY.getId(), "Available"),
		UNITS(TermId.UNITS_INVENTORY.getId(), "Units"),
		LOTS(57, "Lots");

		private static Map<Integer, GERMPLASM_DETAILS_FIELD> byId =
			Arrays.stream(GERMPLASM_DETAILS_FIELD.values())
				.collect(Collectors.toMap(GERMPLASM_DETAILS_FIELD::getId, Function.identity()));

		private final int id;
		private final String name;

		GERMPLASM_DETAILS_FIELD(final int id, final String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static GERMPLASM_DETAILS_FIELD getById(final int id) {
			return byId.get(id);
		}
	}

	// Germplasm Details. fields
	private static LabelType GERMPLASM_DETAILS_FIXED_LABEL_TYPES = new LabelType("Germplasm Details", "Germplasm Details")
		.withFields(Arrays.stream(GERMPLASM_DETAILS_FIELD.values())
			.map(field -> new Field(field.getId(), field.getName()))
			.collect(Collectors.toList()));


	private enum PEDIGREE_FIELD {
		CROSS(58, "Cross"),
		FEMALE_PARENT_GID(59, "Female Parent GID"),
		MALE_PARENT_GID(TermId.CROSS_MALE_GID.getId(), "Male Parent GID"),
		MALE_PARENT_PREFERRED_NAME(TermId.CROSS_MALE_PREFERRED_NAME.getId(), "Male Parent Preferred Name"),
		FEMALE_PARENT_PREFERRED_NAME(TermId.CROSS_FEMALE_PREFERRED_NAME.getId(), "Female Parent Preferred Name"),
		INMEDIATE_SOURCE_GID(62, "Immediate Source GID"),
		INMEDIATE_SOURCE_PREFERRED_NAME(63, "Immediate Source Preferred Name");

		private static Map<Integer, PEDIGREE_FIELD> byId =
			Arrays.stream(PEDIGREE_FIELD.values())
				.collect(Collectors.toMap(PEDIGREE_FIELD::getId, Function.identity()));

		private final int id;
		private final String name;

		PEDIGREE_FIELD(final int id, final String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static PEDIGREE_FIELD getById(final int id) {
			return byId.get(id);
		}
	}

	private static LabelType PEDIGREE_FIXED_LABEL_TYPES = new LabelType("Pedigree", "Pedigree")
		.withFields(Arrays.stream(PEDIGREE_FIELD.values())
			.map(field -> new Field(field.getId(), field.getName()))
			.collect(Collectors.toList()));

	@Override
	void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput) {

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

		final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
			.getSearchRequest(labelsInfoInput.getSearchRequestId(), GermplasmSearchRequest.class);

		final List<UserDefinedField> attributeTypes = this.germplasmSearchService.getGermplasmAttributeTypes(germplasmSearchRequest);
		final List<UserDefinedField> nameTypes = this.germplasmSearchService.getGermplasmNameTypes(germplasmSearchRequest);

		// Germplasm Details labels
		labelTypes.add(GERMPLASM_DETAILS_FIXED_LABEL_TYPES);

		// Pedigree labels
		labelTypes.add(PEDIGREE_FIXED_LABEL_TYPES);

		// Names labels
		final LabelType namesType = new LabelType("Names", "Names");
		namesType.setFields(new ArrayList<>());
		namesType.getFields().addAll(nameTypes.stream()
			.map(attr -> new Field(toKey(attr.getFldno()), attr.getFcode()))
			.collect(Collectors.toList()));
		labelTypes.add(namesType);

		// Attribiutes labels
		final LabelType attirbutesType = new LabelType("Attributes", "Attributes");
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

		final List<Integer> listOfGermplasmDetailsAndPedrigreeIds = Stream.of(PEDIGREE_FIELD.values())
			.map(PEDIGREE_FIELD::getId)
			.collect(Collectors.toList());

		listOfGermplasmDetailsAndPedrigreeIds.addAll(
			Stream.of(GERMPLASM_DETAILS_FIELD.values()).map(GERMPLASM_DETAILS_FIELD::getId).collect(Collectors.toList()));

		final List<Integer> selectedFieldIds =
			labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toList());

		final boolean haveNamesOrAttributes =
			selectedFieldIds.stream().anyMatch((fieldId) -> !listOfGermplasmDetailsAndPedrigreeIds.contains(fieldId));

		PageRequest pageRequest = null;
		if (!StringUtils.isBlank(labelsGeneratorInput.getSortBy())) {
			pageRequest = new PageRequest(0, GermplasmLabelPrinting.MAX_GID_LIST_SIZE, new Sort(Sort.Direction.ASC, labelsGeneratorInput.getSortBy()));
		}

		final List<GermplasmSearchResponse> responseList =
			this.germplasmService.searchGermplasm(germplasmSearchRequest, pageRequest, null);

		Map<Integer, Map<Integer, String>> attributeValues = null;
		Map<Integer, Map<Integer, String>> nameValues = null;

		if (haveNamesOrAttributes) {
			attributeValues = this.germplasmSearchService.getGermplasmAttributeValues(germplasmSearchRequest);
			nameValues = this.germplasmSearchService.getGermplasmNameValues(germplasmSearchRequest);
		}

		// Data to be exported
		final List<Map<Integer, String>> data = new ArrayList<>();
		final Set<Integer> keys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				keys.add(GERMPLASM_DETAILS_FIELD.GUID.getId());
			} else {
				keys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}

		for (final GermplasmSearchResponse germplasmSearchResponse : responseList) {
			data.add(this.getDataRow(keys, germplasmSearchResponse, attributeValues, nameValues));
		}

		return new LabelsData(GERMPLASM_DETAILS_FIELD.GUID.getId(), data);
	}

	private Map<Integer, String> getDataRow(final Set<Integer> keys, final GermplasmSearchResponse germplasmSearchResponse,
		final Map<Integer, Map<Integer, String>> attributeValues, final Map<Integer, Map<Integer, String>> nameValues) {

		final Map<Integer, String> columns = new HashMap<>();
		for (final Integer key : keys) {
			final int id = toId(key);

			if (GERMPLASM_DETAILS_FIELD.getById(id) != null) {
				switch (GERMPLASM_DETAILS_FIELD.getById(id)) {
					case GID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGid(), ""));
						break;

					case GUID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmUUID(), ""));
						break;

					case GROUP_ID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGroupId(), ""));
						break;

					case LOCATION:
						columns.put(key, Objects.toString(germplasmSearchResponse.getLocationName(), ""));
						break;

					case LOCATION_ABBR:
						columns.put(key, Objects.toString(germplasmSearchResponse.getLocationAbbr(), ""));
						break;

					case BREEDING_METHOD_NAME:
						columns.put(key, Objects.toString(germplasmSearchResponse.getMethodName(), ""));
						break;

					case PREFERRED_ID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmPeferredId(), ""));
						break;

					case PREFERRED_NAME:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmPeferredName(), ""));
						break;

					case REFERENCE:
						columns.put(key, Objects.toString(germplasmSearchResponse.getReference(), ""));
						break;

					case CREATION_DATE:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGermplasmDate(), ""));
						break;

					case METHOD_CODE:
						columns.put(key, Objects.toString(germplasmSearchResponse.getMethodCode(), ""));
						break;

					case METHOD_NUMBER:
						columns.put(key, Objects.toString(germplasmSearchResponse.getMethodNumber(), ""));
						break;

					case METHOD_GROUP:
						columns.put(key, Objects.toString(germplasmSearchResponse.getMethodGroup(), ""));
						break;

					case GROUP_SOURCE_GID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGroupSourceGID(), ""));
						break;

					case GROUP_SOURCE_PREFERRED_NAME:
						columns.put(key, Objects.toString(germplasmSearchResponse.getGroupSourcePreferredName(), ""));
						break;
					case AVAILABLE:
						columns.put(key, Objects.toString(germplasmSearchResponse.getAvailableBalance(), ""));
						break;

					case UNITS:
						columns.put(key, Objects.toString(germplasmSearchResponse.getUnit(), ""));
						break;

					case LOTS:
						columns.put(key, Objects.toString(germplasmSearchResponse.getLotCount(), ""));
						break;

					default:
						break;

				}

			} else if (PEDIGREE_FIELD.getById(id) != null) {
				switch (PEDIGREE_FIELD.getById(id)) {
					case CROSS:
						columns.put(key, Objects.toString(germplasmSearchResponse.getPedigreeString(), ""));
						break;

					case FEMALE_PARENT_GID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getFemaleParentGID(), ""));
						break;

					case MALE_PARENT_GID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getMaleParentGID(), ""));
						break;

					case MALE_PARENT_PREFERRED_NAME:
						columns.put(key, Objects.toString(germplasmSearchResponse.getMaleParentPreferredName(), ""));
						break;

					case FEMALE_PARENT_PREFERRED_NAME:
						columns.put(key, Objects.toString(germplasmSearchResponse.getFemaleParentPreferredName(), ""));
						break;

					case INMEDIATE_SOURCE_GID:
						columns.put(key, Objects.toString(germplasmSearchResponse.getImmediateSourceGID(), ""));
						break;

					case INMEDIATE_SOURCE_PREFERRED_NAME:
						columns.put(key, Objects.toString(germplasmSearchResponse.getImmediateSourcePreferredName(), ""));
						break;

					default:
						break;
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
		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.CREATION_DATE.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.GERMPLASM_DATE);
		}

		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.METHOD_CODE.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.METHOD_ABBREV);
		}

		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.METHOD_NUMBER.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.METHOD_NUMBER);
		}

		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.METHOD_GROUP.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.METHOD_GROUP);
		}

		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.PREFERRED_NAME.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.PREFERRED_NAME);
		}

		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.PREFERRED_ID.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.PREFERRED_ID);
		}

		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.GROUP_SOURCE_GID.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.GROUP_SOURCE_GID);
		}

		if (listOfSelectedFields.contains(GERMPLASM_DETAILS_FIELD.GROUP_SOURCE_PREFERRED_NAME.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.GROUP_SOURCE);
		}


		if (listOfSelectedFields.contains(PEDIGREE_FIELD.FEMALE_PARENT_GID.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.MALE_PARENT_GID);
		}

		if (listOfSelectedFields.contains(PEDIGREE_FIELD.MALE_PARENT_GID.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.FEMALE_PARENT_GID);
		}

		if (listOfSelectedFields.contains(PEDIGREE_FIELD.MALE_PARENT_PREFERRED_NAME.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.CROSS_MALE_PREFERRED_NAME);
		}

		if (listOfSelectedFields.contains(PEDIGREE_FIELD.FEMALE_PARENT_PREFERRED_NAME.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.CROSS_FEMALE_PREFERRED_NAME);
		}

		if (listOfSelectedFields.contains(PEDIGREE_FIELD.INMEDIATE_SOURCE_GID.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.IMMEDIATE_SOURCE_GID);
		}

		if (listOfSelectedFields.contains(PEDIGREE_FIELD.INMEDIATE_SOURCE_PREFERRED_NAME.id)) {
			addedColumnsPropertyIds.add(GermplasmLabelPrinting.IMMEDIATE_SOURCE);
		}
	}

	@Override
	List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	@Override
	List<Sortable> getSortableFields() {
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
}