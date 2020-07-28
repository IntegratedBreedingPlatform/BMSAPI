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
		final long lotsWithoutUnitCount = extendedLotDtos.stream().filter(lot -> lot.getUnitId() == null).count();
		if (lotsWithoutUnitCount != 0) {
			errors.reject("selected.lots.with.no.unit", new String[] {String.valueOf(lotsWithoutUnitCount)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateClosedLots(final List<ExtendedLotDto> extendedLotDtos) {
		errors = new MapBindingResult(new HashMap<String, String>(), ExtendedLotDto.class.getName());
		//Validate that none of them are closed
		final long closedLotsCount =
			extendedLotDtos.stream().filter(lot -> lot.getStatus().equalsIgnoreCase(LotStatus.CLOSED.name())).count();
		if (closedLotsCount != 0) {
			errors.reject("selected.lots.closed", new String[] {String.valueOf(closedLotsCount)}, "");
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

}
