package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.inventory.manager.LotService;
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
import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	private SearchRequestService searchRequestService;

	@Autowired
	private LotService lotService;

	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);


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
			.map(field -> new Field(field.getId(), field.getName()))
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
			.map(field -> new Field(field.getId(), field.getName()))
			.collect(Collectors.toList()));

	/**
	 * Identify non-fixed columns with id = MAX_FIXED_TYPE_INDEX + column-id
	 * Requires no collision between non-fixed columns id
	 * Allocates some space for future fixed-columns
	 */
	private static final Integer MAX_FIXED_TYPE_INDEX = 1000;

	static int toKey(final int id) {
		return id + MAX_FIXED_TYPE_INDEX;
	}

	static int toId(final int key) {
		if (key > MAX_FIXED_TYPE_INDEX) {
			return key - MAX_FIXED_TYPE_INDEX;
		}
		return key;
	}

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
		germplasmLabelTypes.setFields(new ArrayList<>(GERMPLASM_FIXED_LABEL_TYPES.getFields()));

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
				.map(attributeVariable -> new Field(toKey(attributeVariable.getId()),
					StringUtils.isNotBlank(attributeVariable.getAlias()) ? attributeVariable.getAlias() : attributeVariable.getName()))
				.collect(Collectors.toList()));
			labelTypes.add(germplasmLabelTypes);
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
		final Map<Integer, Map<Integer, String>> attributeValues = this.lotService.getGermplasmAttributeValues(searchDto);

		// Data to be exported
		final List<Map<Integer, String>> data = new ArrayList<>();

		final Map<String, String> pedigreeByGID = new HashMap<>();
		final Set<Integer> keys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				keys.add(LOT_FIELD.LOT_UID.getId());
			} else {
				keys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}

		for (final ExtendedLotDto extendedLotDto : extendedLotDtos) {
			data.add(this.getDataRow(labelsGeneratorInput, keys, extendedLotDto, attributeValues, pedigreeByGID));
		}

		return new LabelsData(LOT_FIELD.LOT_UID.getId(), data);
	}

	Map<Integer, String> getDataRow(final LabelsGeneratorInput labelsGeneratorInput,
		final Set<Integer> keys,
		final ExtendedLotDto extendedLotDto,
		final Map<Integer, Map<Integer, String>> attributeValues,
		final Map<String, String> pedigreeByGID) {

		final Map<Integer, String> columns = new HashMap<>();

		// Select columns
		for (final Integer key : keys) {
			final int id = toId(key);

			if (LOT_FIELD.getById(id) != null) {
				switch (LOT_FIELD.getById(id)) {
					case LOT_UID:
						columns.put(key, extendedLotDto.getLotUUID());
						break;
					case LOT_ID:
						columns.put(key, Objects.toString(extendedLotDto.getLotId(), ""));
						break;
					case STOCK_ID:
						columns.put(key, extendedLotDto.getStockId());
						break;
					case STATUS:
						columns.put(key, extendedLotDto.getStatus());
						break;
					case STORAGE_LOCATION:
						columns.put(key, extendedLotDto.getLocationName());
						break;
					case UNITS:
						columns.put(key, extendedLotDto.getUnitName());
						break;
					case ACTUAL_BALANCE:
						columns.put(key, Objects.toString(extendedLotDto.getActualBalance(), ""));
						break;
					case AVAILABLE:
						columns.put(key, Objects.toString(extendedLotDto.getAvailableBalance(), ""));
						break;
					case RESERVED:
						columns.put(key, Objects.toString(extendedLotDto.getReservedTotal(), ""));
						break;
					case TOTAL_WITHDRAWALS:
						columns.put(key, Objects.toString(extendedLotDto.getWithdrawalTotal(), ""));
						break;
					case PENDING_DEPOSITS:
						columns.put(key, Objects.toString(extendedLotDto.getPendingDepositsTotal(), ""));
						break;
					case NOTES:
						columns.put(key, extendedLotDto.getNotes());
						break;
					case USERNAME:
						columns.put(key, extendedLotDto.getCreatedByUsername());
						break;
					case CREATION_DATE:
						columns.put(key, Objects.toString(extendedLotDto.getCreatedDate(), ""));
						break;
					case LAST_DEPOSIT_DATE:
						columns.put(key, Objects.toString(extendedLotDto.getLastDepositDate(), ""));
						break;
					case LAST_WITHDRAWAL_DATE:
						columns.put(key, Objects.toString(extendedLotDto.getLastWithdrawalDate(), ""));
						break;
					default:
						break;
				}
			} else if (GERMPLASM_FIELD.getById(id) != null) {
				switch (GERMPLASM_FIELD.getById(id)) {
					case GID:
						columns.put(key, Objects.toString(extendedLotDto.getGid(), ""));
						break;
					case GROUP_ID:
						columns.put(key, Objects.toString(extendedLotDto.getMgid(), ""));
						break;
					case DESIGNATION:
						columns.put(key, Objects.toString(extendedLotDto.getDesignation(), ""));
						break;
					case BREEDING_METHOD:
						columns.put(key, Objects.toString(extendedLotDto.getGermplasmMethodName(), ""));
						break;
					case LOCATION:
						columns.put(key, Objects.toString(extendedLotDto.getGermplasmLocation(), ""));
						break;
					case CROSS:
						columns.put(key, this.getPedigree(Objects.toString(extendedLotDto.getGid(), null), pedigreeByGID));
						break;
					default:
						break;
				}
			} else {
				// Not part of the fixed columns
				// Attributes
				final Map<Integer, String> attributesByType = attributeValues.get(extendedLotDto.getGid());
				if (attributesByType != null) {
					final String attributeValue = attributesByType.get(id);
					if (attributeValue != null) {
						// Truncate attribute values to 200 characters if export file type is PDF
						columns.put(key, FileType.PDF.equals(labelsGeneratorInput.getFileType()) && StringUtils.length(attributeValue) > GermplasmLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH ?
							attributeValue.substring(0, GermplasmLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH) + "..." : attributeValue);
					}
				}

			}
		}

		return columns;
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
