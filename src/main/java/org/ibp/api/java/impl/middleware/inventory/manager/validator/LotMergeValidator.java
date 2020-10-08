package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotMergeRequestDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LotMergeValidator {

	private BindingResult errors;

	public void validateRequest(final LotMergeRequestDto lotMergeRequestDto) {
		BaseValidator.checkNotNull(lotMergeRequestDto,"lot.merge.input.null");
		BaseValidator.checkNotNull(lotMergeRequestDto.getLotUUIDToKeep(),"lot.merge.keep.lot.null");
	}

	public void validate(final String keepLotUUID, final List<ExtendedLotDto> extendedLotDtos){
		errors = new MapBindingResult(new HashMap<String, String>(), LotMergeRequestDto.class.getName());

		this.validateAtLeastTwoLotsToMerge(extendedLotDtos);
		this.validateKeepLotIsSelected(keepLotUUID, extendedLotDtos);

		final AtomicReference<Integer> gid = new AtomicReference<>();
		final AtomicReference<String> unitName = new AtomicReference<>();
		boolean invalidLot = extendedLotDtos.
				stream().
				anyMatch(lot -> {
					//Validate lot is active
					if (!lot.getStatus().equals(LotStatus.ACTIVE.name())) {
						errors.reject("lots.closed", "");
						return true;
					}
					if (Objects.isNull(gid.get())) {
						gid.set(lot.getGid());
					}
					if (Objects.isNull(unitName.get())) {
						unitName.set(lot.getUnitName());
					}
					//Validate lots has the same gids
					if (lot.getGid().intValue() != gid.get().intValue()) {
						errors.reject("lot.merge.gid.different", "");
						return true;
					}
					//Validate lots has the same unit
					if (!unitName.get().equals(lot.getUnitName())) {
						errors.reject("lot.merge.unit.different", "");
						return true;
					}

					//Validate that the discarded lot has amount greater than zero
					if (!keepLotUUID.equals(lot.getLotUUID()) && lot.getActualBalance() <= 0d) {
						errors.reject("lot.merge.discarded.lot.invalid.actual.balance", "");
						return true;
					}

					return false;
				});

		if (invalidLot) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateAtLeastTwoLotsToMerge(final List<ExtendedLotDto> extendedLotDtos) {
		final Set<String> lotUUIDs = new HashSet<>();
		final boolean atLeastTwoLotPresent = extendedLotDtos.stream()
				.anyMatch(extendedLotDto -> {
					lotUUIDs.add(extendedLotDto.getLotUUID());
					if (lotUUIDs.size() == 2) {
						return true;
					}
					return false;
				});
		if (!atLeastTwoLotPresent) {
			errors.reject("lot.merge.one.lot.present", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateKeepLotIsSelected(final String keepLotUUID, final List<ExtendedLotDto> extendedLotDtos) {
		extendedLotDtos.stream()
				.filter(extendedLotDto -> keepLotUUID.equals(extendedLotDto.getLotUUID()))
				.findFirst()
				.orElseThrow(() -> {
					errors.reject("lot.merge.keep.lot.not.selected");
					return new ApiRequestValidationException(errors.getAllErrors());
				});
	}

}
