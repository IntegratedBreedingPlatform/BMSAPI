package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by clarysabel on 2/27/20.
 */
@Component
public class ExtendedLotListValidator {

	private BindingResult errors;

	public void validateEmptyList(final List<ExtendedLotDto> extendedLotDtos) {
		errors = new MapBindingResult(new HashMap<String, String>(), ExtendedLotDto.class.getName());
		if (extendedLotDtos == null || extendedLotDtos.isEmpty()) {
			errors.reject("no.lots.selected", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateEmptyUnits(final List<ExtendedLotDto> extendedLotDtos) {
		errors = new MapBindingResult(new HashMap<String, String>(), ExtendedLotDto.class.getName());
		//Validate that none of them has null unit id
		final boolean lotsWithoutUnit = extendedLotDtos.stream().anyMatch(lot -> lot.getUnitId() == null);
		if (lotsWithoutUnit) {
			errors.reject("lots.with.no.unit",null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateClosedLots(final List<ExtendedLotDto> extendedLotDtos) {
		errors = new MapBindingResult(new HashMap<String, String>(), ExtendedLotDto.class.getName());
		//Validate that none of them are closed
		final boolean closedLot =
			extendedLotDtos.stream().anyMatch(lot -> lot.getStatus().equalsIgnoreCase(LotStatus.CLOSED.name()));
		if (closedLot) {
			errors.reject("lots.closed", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateAllProvidedLotUUIDsExist(final List<ExtendedLotDto> extendedLotDtos, final Set<String> lotUUIDs) {
		errors = new MapBindingResult(new HashMap<String, String>(), ExtendedLotDto.class.getName());
		if (lotUUIDs != null && !lotUUIDs.isEmpty() && lotUUIDs.size() != extendedLotDtos.size()) {
			errors.reject("lots.does.not.exist", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateLotUUIDsDuplicated(final List<ExtendedLotDto> extendedLotDtos, final List<String> lotUUIDs) {
		errors = new MapBindingResult(new HashMap<String, String>(), ExtendedLotDto.class.getName());
		if (lotUUIDs.size() != extendedLotDtos.size()) {
			errors.reject("lots.duplicated", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

}
