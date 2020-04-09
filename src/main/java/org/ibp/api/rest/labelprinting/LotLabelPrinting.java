package org.ibp.api.rest.labelprinting;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.UserDefinedField;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional
public class LotLabelPrinting extends LabelPrintingStrategy {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private LotService lotService;

	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF);


	// (Keeping it TERM_ID agnostic for now)
	// Lot IDs
	private enum LOT_FIELD {
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

		private static Map<Integer, LOT_FIELD> byId =
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
	private static LabelType LOT_FIXED_LABEL_TYPES = new LabelType("Lot", "Lot")
		.withFields(Arrays.stream(LOT_FIELD.values())
			.map(field -> new Field(field.getId(), field.getName()))
			.collect(Collectors.toList()));


	// Germplasm ids
	private enum GERMPLASM_FIELD {
		GID(16, "GID"),
		GROUP_ID(17, "Group ID"),
		DESIGNATION(18, "Designation"),
		BREEDING_METHOD(19, "Breeding method"),
		LOCATION(20, "Location"),
		CROSS(21, "Cross");

		private static Map<Integer, GERMPLASM_FIELD> byId =
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
	private static LabelType GERMPLASM_FIXED_LABEL_TYPES = new LabelType("Germplasm", "Germplasm")
		.withFields(Arrays.stream(GERMPLASM_FIELD.values())
			.map(field -> new Field(field.getId(), field.getName()))
			.collect(Collectors.toList()));

	/**
	 * Identify non-fixed columns with id = MAX_FIXED_TYPE_INDEX + column-id
	 * Requires no collision between non-fixed columns id
	 */
	private static final Integer MAX_FIXED_TYPE_INDEX = Arrays.asList(LOT_FIXED_LABEL_TYPES, GERMPLASM_FIXED_LABEL_TYPES) //
		.stream() //
		.flatMap(labelType -> labelType.getFields().stream()) //
		.max(Comparator.comparing(Field::getId)) //
		.orElseThrow(RuntimeException::new) //
		.getId();

	private static int toKey(final int id) {
		return id + MAX_FIXED_TYPE_INDEX;
	}

	private static int toId(final int key) {
		if (key > MAX_FIXED_TYPE_INDEX) {
			return key - MAX_FIXED_TYPE_INDEX;
		}
		return key;
	}

	@Override
	void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput) {
		if (labelsInfoInput.getSearchRequestId() == null) {
			throw new ApiRequestValidationException(Arrays.asList(
				new ObjectError("", new String[] {"searchrequestid.invalid"}, null, null))
			);
		}
	}

	@Override
	void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput) {

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
	OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput) {
		return new OriginResourceMetadata();
	}

	@Override
	List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput) {

		// Get attributtes
		final Integer searchRequestId = labelsInfoInput.getSearchRequestId();
		final LotsSearchDto searchDto =
			(LotsSearchDto) this.searchRequestService.getSearchRequest(searchRequestId, LotsSearchDto.class);
		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDto, null);
		final List<Integer> gids = extendedLotDtos.stream().map(ExtendedLotDto::getGid).collect(Collectors.toList());
		final List<UserDefinedField> attributes = this.germplasmDataManager.getAttributeTypesByGIDList(gids);

		// Build label list

		final List<LabelType> labelTypes = new LinkedList<>();
		labelTypes.add(LOT_FIXED_LABEL_TYPES);

		// Germplasm labels
		final LabelType germplasmLabelTypes = new LabelType(GERMPLASM_FIXED_LABEL_TYPES.getTitle(), GERMPLASM_FIXED_LABEL_TYPES.getKey());
		germplasmLabelTypes.setFields(GERMPLASM_FIXED_LABEL_TYPES.getFields());
		germplasmLabelTypes.getFields().addAll(attributes.stream()
			.map(attr -> new Field(toKey(attr.getFldno()), attr.getFname()))
			.collect(Collectors.toList()));
		labelTypes.add(germplasmLabelTypes);

		return labelTypes;
	}

	@Override
	LabelsData getLabelsData(final LabelsGeneratorInput labelsGeneratorInput) {
		// Get raw data
		final Integer searchRequestId = labelsGeneratorInput.getSearchRequestId();
		final LotsSearchDto searchDto =
			(LotsSearchDto) this.searchRequestService.getSearchRequest(searchRequestId, LotsSearchDto.class);
		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDto, null);
		final List<Integer> gids = extendedLotDtos.stream().map(ExtendedLotDto::getGid).collect(Collectors.toList());

		// Data to be exported
		final List<Map<Integer, String>> data = new ArrayList<>();

		final List<Integer> keys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toList());

		for (final ExtendedLotDto extendedLotDto : extendedLotDtos) {
			data.add(this.getDataRow(extendedLotDto, keys));
		}

		return new LabelsData(null, data);
	}

	private Map<Integer, String> getDataRow(final ExtendedLotDto extendedLotDto, final List<Integer> keys) {
		final Map<Integer, String> columns = new HashMap<>();

		// Select columns
		for (final Integer key : keys) {
			final int id = toId(key);

			if (LOT_FIELD.getById(id) != null) {
				// TODO complete
				switch (LOT_FIELD.getById(id)) {
					case LOT_UID:
						columns.put(key, extendedLotDto.getLotUUID());
						break;
					default:
						break;
				}
			} else if (GERMPLASM_FIELD.getById(id) != null) {
				// TODO complete
			} else {
				// Not part of the fixed columns
				// TODO complete

			}
		}

		return columns;
	}

	@Override
	List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
