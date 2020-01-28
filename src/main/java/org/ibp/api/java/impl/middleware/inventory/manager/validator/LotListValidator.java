package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LotListValidator {

	private BindingResult errors;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private VariableService variableService;

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));

	public void validate(final List<LotItemDto> lotList) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());

		if (lotList == null) {
			errors.reject("lot.input.list.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Validate that none of the elements in the list is null
		if (countNullElements(lotList) > 0) {
			errors.reject("lot.input.list.item.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateGermplasmList(lotList);
		this.validateStorageLocations(lotList);
		this.validateScaleNames(lotList);
		this.validateStockIds(lotList);
		this.validateInitialAmounts(lotList);
		this.validateComments(lotList);
	}

	private void validateGermplasmList(final List<LotItemDto> lotList) {
		final List<Integer> gids = lotList.stream().map(LotItemDto::getGid).distinct().collect(Collectors.toList());
		if (countNullElements(gids) > 0) {
			errors.reject("lot.input.list.gid.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<Germplasm> existingGermplasms = germplasmDataManager.getGermplasms(gids);
		if (existingGermplasms.size() != gids.size()) {
			final List<Integer> existingGids = existingGermplasms.stream().map(Germplasm::getGid).collect(Collectors.toList());
			final List<Integer> invalidGids = new ArrayList<>(gids);
			invalidGids.removeAll(existingGids);
			errors.reject("lot.input.invalid.gids", new String[] {this.buildErrorMessageFromList(invalidGids)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateStorageLocations(final List<LotItemDto> lotList) {
		final List<String> locationAbbreviations =
				lotList.stream().map(LotItemDto::getStorageLocationAbbr).distinct().collect(Collectors.toList());
		if (countNullElements(locationAbbreviations) > 0) {
			errors.reject("lot.input.list.gid.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		if (countNullOrEmptyStrings(locationAbbreviations)>0) {
			errors.reject("lot.input.list.location.abbreviation.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final List<Location> existingLocations =
				locationDataManager.getFilteredLocations(STORAGE_LOCATION_TYPE, null, null, locationAbbreviations);
		if (existingLocations.size() != locationAbbreviations.size()) {
			final List<String> existingAbbreviations = existingLocations.stream().map(Location::getLabbr).collect(Collectors.toList());
			final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
			invalidAbbreviations.removeAll(existingAbbreviations);
			errors.reject("lot.input.invalid.abbreviations", new String[] {this.buildErrorMessageFromList(invalidAbbreviations)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		
	}

	private void validateScaleNames(final List<LotItemDto> lotList){
		final List<String> scaleNames = lotList.stream().map(LotItemDto::getScaleName).distinct().collect(Collectors.toList());
		if (countNullOrEmptyStrings(scaleNames) > 0) {
			errors.reject("lot.input.list.units.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<VariableDetails> existingInventoryScales = this.variableService.getVariablesByFilter(variableFilter);
		if (existingInventoryScales.size() != scaleNames.size()) {
			final List<String> existingScaleNames = existingInventoryScales.stream().map(VariableDetails::getAlias).collect(Collectors.toList());
			final List<String> invalidScaleNames = new ArrayList<>(scaleNames);
			invalidScaleNames.removeAll(existingScaleNames);
			errors.reject("lot.input.invalid.units", new String[] {this.buildErrorMessageFromList(invalidScaleNames)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateStockIds(final List<LotItemDto> lotList) {
		final List<String> stockIds = lotList.stream().map(LotItemDto::getStockId).distinct().collect(Collectors.toList());
		if (countNullOrEmptyStrings(stockIds) > 0) {
			errors.reject("lot.input.list.stock.ids.null.or.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	private void validateInitialAmounts(final List<LotItemDto> lotList) {

	}

	private void validateComments(final List<LotItemDto> lotList) {

	}

	private <T> String buildErrorMessageFromList(final List<T> elements) {
		final StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(elements.stream().limit(3).map(Object::toString).collect(Collectors.joining(" , ")));

		if (elements.size() > 3) {
			stringBuilder.append(" and ").append(elements.size() - 3).append(" more");
		}

		return stringBuilder.toString();
	}

	private <T> long countNullElements(final List<T> list) {
		return list.stream().filter(Objects::isNull).count();
	}

	private long countNullOrEmptyStrings(final List<String> list) {
		return list.stream().filter(s -> StringUtils.isEmpty(s)).count();
	}

}
