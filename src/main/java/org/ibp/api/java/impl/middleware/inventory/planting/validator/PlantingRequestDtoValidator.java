package org.ibp.api.java.impl.middleware.inventory.planting.validator;

import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.service.impl.inventory.PlantingPreparationDTO;
import org.ibp.api.Util;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PlantingRequestDtoValidator {

	private BindingResult errors;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.PlantingService plantingService;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Autowired
	private VariableService variableService;

	public void validatePlantingRequestDto(final Integer studyId, final Integer datasetId, final PlantingRequestDto plantingRequestDto) {
		errors = new MapBindingResult(new HashMap<String, String>(), PlantingRequestDto.class.getName());

		if (plantingRequestDto == null) {
			errors.reject("planting.request.null", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		searchCompositeDtoValidator.validateSearchCompositeDto(plantingRequestDto.getSelectedObservationUnits(), errors);

		final PlantingPreparationDTO plantingPreparationDTO =
			plantingService.searchPlantingPreparation(studyId, datasetId, plantingRequestDto.getSelectedObservationUnits());
		if (plantingPreparationDTO.getEntries().isEmpty()) {
			errors.reject("planting.preparation.empty", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (plantingRequestDto.getLotPerEntryNo() == null || plantingRequestDto.getLotPerEntryNo().isEmpty()) {
			errors.reject("planting.lot.per.entry.no.null.or.empty", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (plantingRequestDto.getWithdrawalsPerUnit() == null || plantingRequestDto.getWithdrawalsPerUnit().isEmpty()) {
			errors.reject("planting.withdrawals.per.unit.null.or.empty", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.inventoryCommonValidator.validateUnitNames(new ArrayList<>(plantingRequestDto.getWithdrawalsPerUnit().keySet()), errors);

		plantingRequestDto.getWithdrawalsPerUnit().forEach((k, v) -> {
				if (!v.isValid()) {
					errors.reject("planting.invalid.withdrawal.instruction", new String[] {k}, "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
			}
		);

		final Set<Integer> requestedEntryNos =
			plantingRequestDto.getLotPerEntryNo().stream().map(PlantingRequestDto.LotEntryNumber::getEntryNo).collect(
				Collectors.toSet());
		if (requestedEntryNos.size() != plantingRequestDto.getLotPerEntryNo().size()) {
			errors.reject("planting.repeated.entry.no", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<PlantingPreparationDTO.PlantingPreparationEntryDTO> filteredPlantingPreparation =
			plantingPreparationDTO.getEntries().stream().filter(e -> requestedEntryNos.contains(e.getEntryNo())).collect(
				Collectors.toList());

		if (filteredPlantingPreparation.size() != requestedEntryNos.size()) {
			errors.reject("planting.entry.no.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<Integer> selectedLotsUnitIds = new ArrayList<>();
		for (final PlantingRequestDto.LotEntryNumber lotEntryNumber : plantingRequestDto.getLotPerEntryNo()) {
			final PlantingPreparationDTO.PlantingPreparationEntryDTO plantingPreparationEntryDTO =
				filteredPlantingPreparation.stream().filter(e -> lotEntryNumber.getEntryNo().equals(e.getEntryNo()))
					.collect(Collectors.toList()).get(0);
			final Optional<PlantingPreparationDTO.PlantingPreparationEntryDTO.StockDTO>
				stockDTO = plantingPreparationEntryDTO.getStockByStockId().values().stream()
				.filter(s -> lotEntryNumber.getLotId().equals(s.getLotId())).findFirst();
			if (!stockDTO.isPresent()) {
				errors.reject("planting.lot.entry.no.invalid",
					new String[] {String.valueOf(lotEntryNumber.getLotId()), String.valueOf(lotEntryNumber.getEntryNo())}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			selectedLotsUnitIds.add(stockDTO.get().getUnitId());
		}

		final VariableFilter variableFilterById = new VariableFilter();
		variableFilterById.addVariableIds(selectedLotsUnitIds);
		final List<String> selectedUnitNames =
			this.variableService.getVariablesByFilter(variableFilterById).stream().map(VariableDetails::getName)
				.collect(Collectors.toList());
		if (!plantingRequestDto.getWithdrawalsPerUnit().keySet().containsAll(selectedUnitNames)) {
			final List<String> invalidUnitNames = new ArrayList<>(selectedUnitNames);
			invalidUnitNames.removeAll(plantingRequestDto.getWithdrawalsPerUnit().keySet());
			errors.reject("planting.missing.unit.instructions", new String[] {Util.buildErrorMessageFromList(invalidUnitNames, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.inventoryCommonValidator.validateTransactionNotes(plantingRequestDto.getNotes(), errors);

	}

}
