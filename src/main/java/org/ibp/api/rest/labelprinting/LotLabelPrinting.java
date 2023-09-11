package org.ibp.api.rest.labelprinting;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.labelprinting.LabelPrintingPresetDTO;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.inventory.LotAttributeService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.inventory.manager.LotService;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional
public class LotLabelPrinting extends LabelPrintingStrategy {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private LotAttributeService lotAttributeService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private LotService lotService;

	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	private Set<Integer> lotAttributeKeys;


	// Lot IDs
	// (Keeping it TERM_ID agnostic for now)
	// TODO generic lot variable columns query
	private enum LOT_FIELD {
		LOT_ID(22, "Lot ID"), // Added later
		LOT_UID(1, "Lot UID"),
		STOCK_ID(2, "Stock id"),
		STATUS(3, "Status"),
		STORAGE_LOCATION(4, "Storage location"),
		UNITS(5, "Units"),
		ACTUAL_BALANCE(6, "Actual balance"),
		AVAILABLE(7, "Available"),
		RESERVED(8, "Reserved"),
		TOTAL_WITHDRAWALS(9, "Total withdrawals"),
		PENDING_DEPOSITS(10, "Pending deposits"),
		NOTES(11, "Notes"),
		USERNAME(12, "Username"),
		CREATION_DATE(13, "Creation date"),
		LAST_DEPOSIT_DATE(14, "Last deposit date"),
		LAST_WITHDRAWAL_DATE(15, "Last withdrawal date");

		private static final Map<Integer, LOT_FIELD> byId =
			Arrays.stream(LOT_FIELD.values()).collect(Collectors.toMap(LOT_FIELD::getId, Function.identity()));

		private final int id;
		private final String name;

		LOT_FIELD(final int id, final String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static LOT_FIELD getById(final int id) {
			return byId.get(id);
		}
	}


	// Lot fields
	private static final LabelType LOT_FIXED_LABEL_TYPES = new LabelType("Lot", "Lot")
		.withFields(Arrays.stream(LOT_FIELD.values())
			.map(field -> new Field(FieldType.STATIC, field.getId(), field.getName()))
			.collect(Collectors.toList()));


	// Germplasm ids
	// (Keeping it TERM_ID agnostic for now)
	private enum GERMPLASM_FIELD {
		GID(16, "GID"),
		GROUP_ID(17, "Group ID"),
		DESIGNATION(18, "Designation"),
		BREEDING_METHOD(19, "Breeding method"),
		LOCATION(20, "Location"),
		CROSS(21, "Cross")
		// LOT_ID(22) in lot group
		;

		private static final Map<Integer, GERMPLASM_FIELD> byId =
			Arrays.stream(GERMPLASM_FIELD.values()).collect(Collectors.toMap(GERMPLASM_FIELD::getId, Function.identity()));

		private final int id;
		private final String name;

		GERMPLASM_FIELD(final int id, final String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static GERMPLASM_FIELD getById(final int id) {
			return byId.get(id);
		}
	}


	// Germplasm fields
	private static final LabelType GERMPLASM_FIXED_LABEL_TYPES = new LabelType("Germplasm", "Germplasm")
		.withFields(Arrays.stream(GERMPLASM_FIELD.values())
			.map(field -> new Field(FieldType.STATIC, field.getId(), field.getName()))
			.collect(Collectors.toList()));

	@Override
	void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		if (labelsInfoInput.getSearchRequestId() == null) {
			throw new ApiRequestValidationException(Arrays.asList(
				new ObjectError("", new String[] {"searchrequestid.invalid"}, null, null))
			);
		}
	}

	@Override
	void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		super.validateLabelsGeneratorInputData(labelsGeneratorInput, programUUID);
	}

	@Override
	public LabelPrintingPresetDTO getDefaultSetting(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		return null;
	}

	@Override
	LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsInfoInput labelsInfoInput) {
		throw new NotSupportedException(new ObjectError("", new String[] {"not.supported"}, null, null));
	}

	@Override
	LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary) {
		throw new NotSupportedException(new ObjectError("", new String[] {"not.supported"}, null, null));
	}

	@Override
	OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final String fileName = FileNameGenerator.generateFileName("lot-labels");
		return new OriginResourceMetadata(FileUtils.cleanFileName(fileName), new HashMap<>());
	}

	@Override
	List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput, final String programUUID) {

		// Build label list
		final List<LabelType> labelTypes = new LinkedList<>();
		labelTypes.add(LOT_FIXED_LABEL_TYPES);

		// Germplasm labels
		final LabelType germplasmLabelTypes = new LabelType(GERMPLASM_FIXED_LABEL_TYPES.getTitle(), GERMPLASM_FIXED_LABEL_TYPES.getKey());
		List<Field> fields = new ArrayList<>(GERMPLASM_FIXED_LABEL_TYPES.getFields());
		if (!SecurityUtil.hasAnyAuthority(PermissionsEnum.VIEW_PEDIGREE_INFORMATION_PERMISSIONS)) {
			fields.removeIf(field -> field.getId().equals(GERMPLASM_FIELD.CROSS.getId()));
		}

		germplasmLabelTypes.setFields(fields);

		// Get attributtes
		final Integer searchRequestId = labelsInfoInput.getSearchRequestId();
		final LotsSearchDto searchDto =
			(LotsSearchDto) this.searchRequestService.getSearchRequest(searchRequestId, LotsSearchDto.class);
		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLotsApplyExportResultsLimit(searchDto, null);

		if (!extendedLotDtos.isEmpty()) {
			final Set<Integer> gids = extendedLotDtos.stream().map(ExtendedLotDto::getGid).collect(Collectors.toSet());
			final List<Variable> germplasmAttributeVariables =
				this.germplasmAttributeService.getGermplasmAttributeVariables(gids.stream().collect(Collectors.toList()), programUUID);
			germplasmLabelTypes.getFields().addAll(germplasmAttributeVariables.stream()
				.map(attributeVariable -> new Field(FieldType.VARIABLE, attributeVariable.getId(),
					StringUtils.isNotBlank(attributeVariable.getAlias()) ? attributeVariable.getAlias() : attributeVariable.getName()))
				.collect(Collectors.toList()));
			labelTypes.add(germplasmLabelTypes);
			this.populateLotAttributesLabelType(programUUID, labelTypes, extendedLotDtos);
		} else {
			this.populateAttributesLabelType(programUUID, labelTypes, Collections.emptyList(), Collections.emptyList());
		}

		return labelTypes;
	}

	@Override
	LabelsData getLabelsData(final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		// Get raw data
		final Integer searchRequestId = labelsGeneratorInput.getSearchRequestId();
		final LotsSearchDto searchDto =
			(LotsSearchDto) this.searchRequestService.getSearchRequest(searchRequestId, LotsSearchDto.class);
		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLotsApplyExportResultsLimit(searchDto, null);
		final Map<Integer, Map<Integer, String>> germplasmAttributeValues = this.lotService.getGermplasmAttributeValues(searchDto);
		final Map<Integer, Map<Integer, String>> lotAttributeValues =
			this.lotAttributeService.getAttributesByLotIdsMap(extendedLotDtos.stream().map(LotDto::getLotId).collect(
				Collectors.toList()));

		// Data to be exported
		final List<Map<String, String>> data = new ArrayList<>();

		final Map<String, String> pedigreeByGID = new HashMap<>();
		final Set<String> combinedKeys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				combinedKeys.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, LOT_FIELD.LOT_UID.getId()));
			} else {
				combinedKeys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}

		final boolean isPdf = FileType.PDF.equals(labelsGeneratorInput.getFileType());

		for (final ExtendedLotDto extendedLotDto : extendedLotDtos) {
			data.add(
				this.getDataRow(isPdf, combinedKeys, extendedLotDto, germplasmAttributeValues, lotAttributeValues, pedigreeByGID));
		}
		return new LabelsData(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, LOT_FIELD.LOT_UID.getId()), data);
	}

	Map<String, String> getDataRow(final boolean isPdf,
		final Set<String> combinedKeys,
		final ExtendedLotDto extendedLotDto,
		final Map<Integer, Map<Integer, String>> germplasmAttributeValues,
		final Map<Integer, Map<Integer, String>> lotAttributeValues,
		final Map<String, String> pedigreeByGID) {

		final Map<String, String> columns = new HashMap<>();

		// Select columns
		for (final String combinedKey : combinedKeys) {
			final FieldType fieldType = FieldType.find(LabelPrintingFieldUtils.getFieldTypeNameFromCombinedKey(combinedKey));

			if (FieldType.VARIABLE.equals(fieldType)) {
				this.putVariableValueInColumns(columns, isPdf, combinedKey, extendedLotDto, germplasmAttributeValues, lotAttributeValues);
			} else if (FieldType.STATIC.equals(fieldType)) {
				this.putStaticValueInColumns(columns, combinedKey, extendedLotDto, pedigreeByGID);
			}
		}

		return columns;
	}

	private void putStaticValueInColumns(final Map<String, String> columns, final String combinedKey, final ExtendedLotDto extendedLotDto,
		final Map<String, String> pedigreeByGID) {
		final Integer fieldId = LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey);
		if (LOT_FIELD.getById(fieldId) != null) {
			switch (LOT_FIELD.getById(fieldId)) {
				case LOT_UID:
					columns.put(combinedKey, extendedLotDto.getLotUUID());
					break;
				case LOT_ID:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getLotId(), ""));
					break;
				case STOCK_ID:
					columns.put(combinedKey, extendedLotDto.getStockId());
					break;
				case STATUS:
					columns.put(combinedKey, extendedLotDto.getStatus());
					break;
				case STORAGE_LOCATION:
					columns.put(combinedKey, extendedLotDto.getLocationName());
					break;
				case UNITS:
					columns.put(combinedKey, extendedLotDto.getUnitName());
					break;
				case ACTUAL_BALANCE:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getActualBalance(), ""));
					break;
				case AVAILABLE:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getAvailableBalance(), ""));
					break;
				case RESERVED:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getReservedTotal(), ""));
					break;
				case TOTAL_WITHDRAWALS:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getWithdrawalTotal(), ""));
					break;
				case PENDING_DEPOSITS:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getPendingDepositsTotal(), ""));
					break;
				case NOTES:
					columns.put(combinedKey, extendedLotDto.getNotes());
					break;
				case USERNAME:
					columns.put(combinedKey, extendedLotDto.getCreatedByUsername());
					break;
				case CREATION_DATE:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getCreatedDate(), ""));
					break;
				case LAST_DEPOSIT_DATE:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getLastDepositDate(), ""));
					break;
				case LAST_WITHDRAWAL_DATE:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getLastWithdrawalDate(), ""));
					break;
				default:
			}
		} else if (GERMPLASM_FIELD.getById(fieldId) != null) {
			switch (GERMPLASM_FIELD.getById(fieldId)) {
				case GID:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getGid(), ""));
					break;
				case GROUP_ID:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getMgid(), ""));
					break;
				case DESIGNATION:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getDesignation(), ""));
					break;
				case BREEDING_METHOD:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getGermplasmMethodName(), ""));
					break;
				case LOCATION:
					columns.put(combinedKey, Objects.toString(extendedLotDto.getGermplasmLocation(), ""));
					break;
				case CROSS:
					columns.put(combinedKey, this.getPedigree(Objects.toString(extendedLotDto.getGid(), null), pedigreeByGID));
					break;
				default:
			}
		}
	}

	void putVariableValueInColumns(final Map<String, String> columns, final boolean isPdf,
		final String combinedKey, final ExtendedLotDto extendedLotDto,
		final Map<Integer, Map<Integer, String>> germplasmAttributeValues,
		final Map<Integer, Map<Integer, String>> lotAttributeValues) {
		final Integer fieldId = LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey);

		if (CollectionUtils.isNotEmpty(this.lotAttributeKeys) && this.lotAttributeKeys.contains(fieldId)) {
			this.addAttributeColumns(isPdf, columns, combinedKey, lotAttributeValues.get(extendedLotDto.getLotId()));
		} else {
			// Not part of the fixed columns or lot attributes
			// Attributes
			this.addAttributeColumns(isPdf, columns, combinedKey, germplasmAttributeValues.get(extendedLotDto.getGid()));
		}
	}

	private void addAttributeColumns(final boolean isPdf, final Map<String, String> columns,
		final String combinedKey, final Map<Integer, String> attributesByType) {
		if (attributesByType != null) {
			final Integer fieldId = LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey);
			final String attributeValue = attributesByType.get(fieldId);
			if (attributeValue != null) {
				// Truncate attribute values to 200 characters if export file type is PDF
				columns.put(combinedKey, isPdf && StringUtils.length(attributeValue) > GermplasmLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH ?
					attributeValue.substring(0, GermplasmLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH) + "..." : attributeValue);
			}
		}
	}

	void populateLotAttributesLabelType(
		final String programUUID, final List<LabelType> labelTypes, final List<ExtendedLotDto> lotDtos) {
		final List<Integer> lotIds = lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toList());
		final List<Variable> attributeVariables = this.lotAttributeService.getLotAttributeVariables(lotIds, programUUID);

		this.populateAttributesLabelType(programUUID, labelTypes, lotIds, attributeVariables);
		this.lotAttributeKeys = (attributeVariables.stream()
			.map(var -> var.getId()).collect(Collectors.toSet()));
	}

	@Override
	List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	@Override
	List<SortableFieldDto> getSortableFields() {
		return null;
	}

	String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
