package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.Util;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.ontology.VariableService;
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

	private static Integer NOTES_MAX_LENGTH = 255;

	private static Integer STOCK_ID_MAX_LENGTH = 35;

	private static final Integer PREFIX_MAX_LENGTH = 15;

	private BindingResult errors;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private VariableService variableService;

	@Autowired
	private LotService lotService;

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));

	private static final String STOCK_ID_PREFIX_REGEXP = "[a-zA-Z0-9]{1,14}[a-zA-Z]";

	public void validate(final LotImportRequestDto lotImportRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());

		if (lotImportRequestDto == null) {
			errors.reject("lot.import.request.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Validate Stock Prefix
		if (lotImportRequestDto.getStockIdPrefix() != null && lotImportRequestDto.getStockIdPrefix().length() > PREFIX_MAX_LENGTH) {
			this.errors.reject("lot.stock.prefix.invalid.length", new String[] {String.valueOf(PREFIX_MAX_LENGTH)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (lotImportRequestDto.getStockIdPrefix() != null && !lotImportRequestDto.getStockIdPrefix().matches(STOCK_ID_PREFIX_REGEXP)) {
			this.errors.reject("lot.stock.prefix.invalid.pattern", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

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
		this.validateStorageLocations(lotList);
		this.validateScaleNames(lotList);
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

		final List<Germplasm> existingGermplasms = germplasmDataManager.getGermplasms(gids);
		if (existingGermplasms.size() != gids.size() || existingGermplasms.stream().filter(g -> g.getDeleted()).count() > 0) {
			final List<Integer> existingGids =
				existingGermplasms.stream().filter(g -> !g.getDeleted()).map(Germplasm::getGid).collect(Collectors.toList());
			final List<Integer> invalidGids = new ArrayList<>(gids);
			invalidGids.removeAll(existingGids);
			errors.reject("lot.input.invalid.gids", new String[] {Util.buildErrorMessageFromList(invalidGids, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateStorageLocations(final List<LotItemDto> lotList) {
		final List<String> locationAbbreviations =
				lotList.stream().map(LotItemDto::getStorageLocationAbbr).distinct().collect(Collectors.toList());
		if (Util.countNullOrEmptyStrings(locationAbbreviations)>0) {
			errors.reject("lot.input.list.location.abbreviation.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final List<Location> existingLocations =
				locationDataManager.getFilteredLocations(STORAGE_LOCATION_TYPE, null, locationAbbreviations);
		if (existingLocations.size() != locationAbbreviations.size()) {
			final List<String> existingAbbreviations = existingLocations.stream().map(Location::getLabbr).collect(Collectors.toList());
			final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
			invalidAbbreviations.removeAll(existingAbbreviations);
			errors.reject("lot.input.invalid.abbreviations", new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		
	}

	private void validateScaleNames(final List<LotItemDto> lotList){
		final List<String> scaleNames = lotList.stream().map(LotItemDto::getScaleName).distinct().collect(Collectors.toList());
		if (Util.countNullOrEmptyStrings(scaleNames) > 0) {
			errors.reject("lot.input.list.units.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<VariableDetails> existingInventoryScales = this.variableService.getVariablesByFilter(variableFilter);
		final List<String> existingScaleNames = existingInventoryScales.stream().map(VariableDetails::getName).collect(Collectors.toList());

		if (!existingScaleNames.containsAll(scaleNames)) {
			final List<String> invalidScaleNames = new ArrayList<>(scaleNames);
			invalidScaleNames.removeAll(existingScaleNames);
			errors.reject("lot.input.invalid.units", new String[] {Util.buildErrorMessageFromList(invalidScaleNames, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateStockIds(final List<LotItemDto> lotList) {
		final List<String> uniqueNotNullStockIds =
			lotList.stream().map(LotItemDto::getStockId).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());

		if (uniqueNotNullStockIds.stream().filter(c -> c.length() > STOCK_ID_MAX_LENGTH).count() > 0) {
			errors.reject("lot.stock.id.length.higher.than.maximum", new String[]{String.valueOf(STOCK_ID_MAX_LENGTH)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final List<String> allStockIds = lotList.stream().map(LotItemDto::getStockId).collect(Collectors.toList());
		if (allStockIds.size() != uniqueNotNullStockIds.size()) {
			errors.reject("lot.input.list.stock.ids.duplicated","");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final List<LotDto> existingLotDtos = this.lotService.getLotsByStockIds(uniqueNotNullStockIds);
		if (!existingLotDtos.isEmpty()){
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
		if (negativeOrZeroValues>0) {
			errors.reject("lot.input.list.initial.balances.negative.values", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNotes(final List<LotItemDto> lotList) {
		final List<String> notes = lotList.stream().map(LotItemDto::getNotes).distinct().collect(Collectors.toList());
		if (notes.stream().filter(c -> c != null && c.length() > NOTES_MAX_LENGTH).count()>0) {
			errors.reject("lot.input.list.notes.length", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
