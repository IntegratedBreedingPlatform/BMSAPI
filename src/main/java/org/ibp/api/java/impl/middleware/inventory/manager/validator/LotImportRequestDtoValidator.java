package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LotImportRequestDtoValidator {

	private static Integer STOCK_ID_MAX_LENGTH = 35;

	private BindingResult errors;

	@Autowired
	private LocationService locationService;

	@Autowired
	private LotService lotService;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	public GermplasmValidator germplasmValidator;

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));

	public void validate(final String programUUID, final LotImportRequestDto lotImportRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());

		if (lotImportRequestDto == null) {
			errors.reject("lot.import.request.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Validate Stock Prefix
		inventoryCommonValidator.validateStockIdPrefix(lotImportRequestDto.getStockIdPrefix(), errors);

		final List<LotItemDto> lotList = lotImportRequestDto.getLotList();

		if (lotList == null) {
			errors.reject("lot.input.list.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (lotList.isEmpty()) {
			errors.reject("lot.input.list.no.items", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Validate that none of the elements in the list is null
		if (Util.countNullElements(lotList) > 0) {
			errors.reject("lot.input.list.item.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateGermplasmList(lotList);
		this.validateStorageLocations(programUUID, lotList);
		this.validateUnitNames(lotList);
		this.validateStockIds(lotList);
		this.validateInitialBalances(lotList);
		this.validateNotes(lotList);
	}

	private void validateGermplasmList(final List<LotItemDto> lotList) {
		final List<Integer> gids = lotList.stream().map(LotItemDto::getGid).distinct().collect(Collectors.toList());
		if (Util.countNullElements(gids) > 0) {
			errors.reject("lot.input.list.gid.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.germplasmValidator.validateGids(this.errors, gids);
	}

	private void validateStorageLocations(final String programUUID, final List<LotItemDto> lotList) {
		final List<String> locationAbbreviations =
			lotList.stream().map(LotItemDto::getStorageLocationAbbr).distinct().collect(Collectors.toList());
		if (Util.countNullOrEmptyStrings(locationAbbreviations) > 0) {
			errors.reject("lot.input.list.location.abbreviation.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<Location> existingLocations =
			this.locationService.getFilteredLocations(
				new LocationSearchRequest(null, STORAGE_LOCATION_TYPE, null, locationAbbreviations, null),null);
		if (existingLocations.size() != locationAbbreviations.size()) {
			final List<String> existingAbbreviations = existingLocations.stream().map(Location::getLabbr).collect(Collectors.toList());
			final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
			invalidAbbreviations.removeAll(existingAbbreviations);
			errors.reject("lot.input.invalid.abbreviations", new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	private void validateUnitNames(final List<LotItemDto> lotList) {
		final List<String> unitNames = lotList.stream().map(LotItemDto::getUnitName).distinct().collect(Collectors.toList());
		if (Util.countNullOrEmptyStrings(unitNames) > 0) {
			errors.reject("lot.input.list.units.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		this.inventoryCommonValidator.validateUnitNames(unitNames, errors);
	}

	private void validateStockIds(final List<LotItemDto> lotList) {
		final List<String> uniqueNotNullStockIds =
			lotList.stream().map(LotItemDto::getStockId).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());

		if (uniqueNotNullStockIds.stream().filter(c -> c.length() > STOCK_ID_MAX_LENGTH).count() > 0) {
			errors.reject("lot.stock.id.length.higher.than.maximum", new String[] {String.valueOf(STOCK_ID_MAX_LENGTH)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final List<String> allStockIds =
			lotList.stream().map(LotItemDto::getStockId).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
		if (allStockIds.size() != uniqueNotNullStockIds.size()) {
			errors.reject("lot.input.list.stock.ids.duplicated", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final List<LotDto> existingLotDtos = this.lotService.getLotsByStockIds(uniqueNotNullStockIds);
		if (!existingLotDtos.isEmpty()) {
			final List<String> existingStockIds = existingLotDtos.stream().map(LotDto::getStockId).collect(Collectors.toList());
			errors.reject("lot.input.list.stock.ids.invalid", new String[] {Util.buildErrorMessageFromList(existingStockIds, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateInitialBalances(final List<LotItemDto> lotList) {
		final List<Double> initialBalances = lotList.stream().map(LotItemDto::getInitialBalance).collect(Collectors.toList());
		if (Util.countNullElements(initialBalances) > 0) {
			errors.reject("lot.input.list.initial.balances.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final long negativeOrZeroValues = initialBalances.stream().filter(d -> d <= 0).count();
		if (negativeOrZeroValues > 0) {
			errors.reject("lot.input.list.initial.balances.negative.values", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNotes(final List<LotItemDto> lotList) {
		final List<String> notes = lotList.stream().map(LotItemDto::getNotes).distinct().collect(Collectors.toList());
		notes.stream().forEach(n -> inventoryCommonValidator.validateLotNotes(n, errors));
	}

}
