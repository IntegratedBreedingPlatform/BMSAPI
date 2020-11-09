package org.ibp.api.java.impl.middleware.inventory.planting.validator;

import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.service.impl.inventory.PlantingPreparationDTO;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PlantingRequestDtoValidatorTest {

	@Mock
	private org.generationcp.middleware.service.api.inventory.PlantingService plantingService;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Mock
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Mock
	private VariableService variableService;

	@InjectMocks
	private PlantingRequestDtoValidator plantingRequestDtoValidator;

	private static final Integer STUDY_ID = 1;

	private static final Integer DATASET_ID = 1;

	@Test
	public void validatePlantingRequestDto_NullPlantingRequestDto_ExceptionThrown() {
		try {
			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.request.null"));
		}
	}

	@Test
	public void validatePlantingRequestDto_PlantingPreparationEmpty_ExceptionThrown() {
		try {
			final PlantingPreparationDTO plantingPreparationDTO = new PlantingPreparationDTO();
			plantingPreparationDTO.setEntries(new ArrayList<>());
			final PlantingRequestDto plantingRequestDto = new PlantingRequestDto();
			plantingRequestDto.setSelectedObservationUnits(new SearchCompositeDto<>());
			Mockito
				.when(this.plantingService
					.searchPlantingPreparation(Mockito.eq(STUDY_ID), Mockito.eq(DATASET_ID), Mockito.any(SearchCompositeDto.class)))
				.thenReturn(plantingPreparationDTO);
			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, plantingRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.preparation.empty"));
		}
	}

	@Test
	public void validatePlantingRequestDto_PlantingLorPerEntryNull_ExceptionThrown() {
		try {
			final PlantingPreparationDTO plantingPreparationDTO = new PlantingPreparationDTO();
			final PlantingPreparationDTO.PlantingPreparationEntryDTO entryDTO = new PlantingPreparationDTO.PlantingPreparationEntryDTO();
			plantingPreparationDTO.setEntries(Collections.singletonList(entryDTO));
			final PlantingRequestDto plantingRequestDto = new PlantingRequestDto();
			plantingRequestDto.setSelectedObservationUnits(new SearchCompositeDto<>());
			Mockito
				.when(this.plantingService
					.searchPlantingPreparation(Mockito.eq(STUDY_ID), Mockito.eq(DATASET_ID), Mockito.any(SearchCompositeDto.class)))
				.thenReturn(plantingPreparationDTO);
			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, plantingRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.lot.per.entry.no.null.or.empty"));
		}
	}

	@Test
	public void validatePlantingRequestDto_WithdrawalInstructionNull_ExceptionThrown() {
		try {
			final PlantingPreparationDTO plantingPreparationDTO = new PlantingPreparationDTO();
			final PlantingPreparationDTO.PlantingPreparationEntryDTO entryDTO = new PlantingPreparationDTO.PlantingPreparationEntryDTO();
			plantingPreparationDTO.setEntries(Collections.singletonList(entryDTO));
			final PlantingRequestDto plantingRequestDto = new PlantingRequestDto();
			plantingRequestDto.setSelectedObservationUnits(new SearchCompositeDto<>());
			final PlantingRequestDto.LotEntryNumber lotEntryNumber = new PlantingRequestDto.LotEntryNumber();
			plantingRequestDto.setLotPerEntryNo(Collections.singletonList(lotEntryNumber));
			Mockito
				.when(this.plantingService
					.searchPlantingPreparation(Mockito.eq(STUDY_ID), Mockito.eq(DATASET_ID), Mockito.any(SearchCompositeDto.class)))
				.thenReturn(plantingPreparationDTO);
			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, plantingRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.withdrawals.per.unit.null.or.empty"));
		}
	}

	@Test
	public void validatePlantingRequestDto_InvalidWithdrawalInstruction_ExceptionThrown() {
		try {
			final PlantingPreparationDTO plantingPreparationDTO = new PlantingPreparationDTO();
			final PlantingPreparationDTO.PlantingPreparationEntryDTO entryDTO = new PlantingPreparationDTO.PlantingPreparationEntryDTO();
			plantingPreparationDTO.setEntries(Collections.singletonList(entryDTO));
			final PlantingRequestDto plantingRequestDto = new PlantingRequestDto();
			plantingRequestDto.setSelectedObservationUnits(new SearchCompositeDto<>());
			final PlantingRequestDto.WithdrawalInstruction withdrawalInstruction = new PlantingRequestDto.WithdrawalInstruction();
			withdrawalInstruction.setGroupTransactions(false);
			withdrawalInstruction.setWithdrawAllAvailableBalance(true);
			final Map<String, PlantingRequestDto.WithdrawalInstruction> withdrawalPerUnit = new HashMap<>();
			withdrawalPerUnit.put("Seed_amount_g", withdrawalInstruction);
			plantingRequestDto.setWithdrawalsPerUnit(withdrawalPerUnit);
			final PlantingRequestDto.LotEntryNumber lotEntryNumber = new PlantingRequestDto.LotEntryNumber();
			plantingRequestDto.setLotPerEntryNo(Collections.singletonList(lotEntryNumber));
			Mockito
				.when(this.plantingService
					.searchPlantingPreparation(Mockito.eq(STUDY_ID), Mockito.eq(DATASET_ID), Mockito.any(SearchCompositeDto.class)))
				.thenReturn(plantingPreparationDTO);
			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, plantingRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.invalid.withdrawal.instruction"));
		}
	}

	@Test
	public void validatePlantingRequestDto_RepeatedEntryNo_ExceptionThrown() {
		try {
			final PlantingPreparationDTO plantingPreparationDTO = new PlantingPreparationDTO();
			final PlantingPreparationDTO.PlantingPreparationEntryDTO entryDTO = new PlantingPreparationDTO.PlantingPreparationEntryDTO();
			plantingPreparationDTO.setEntries(Collections.singletonList(entryDTO));
			final PlantingRequestDto plantingRequestDto = new PlantingRequestDto();
			plantingRequestDto.setSelectedObservationUnits(new SearchCompositeDto<>());
			final PlantingRequestDto.WithdrawalInstruction withdrawalInstruction = new PlantingRequestDto.WithdrawalInstruction();
			withdrawalInstruction.setGroupTransactions(true);
			withdrawalInstruction.setWithdrawAllAvailableBalance(true);
			final Map<String, PlantingRequestDto.WithdrawalInstruction> withdrawalPerUnit = new HashMap<>();
			withdrawalPerUnit.put("Seed_amount_g", withdrawalInstruction);
			plantingRequestDto.setWithdrawalsPerUnit(withdrawalPerUnit);
			final PlantingRequestDto.LotEntryNumber lotEntryNumber1 = new PlantingRequestDto.LotEntryNumber();
			lotEntryNumber1.setEntryNo(1);
			final PlantingRequestDto.LotEntryNumber lotEntryNumber2 = new PlantingRequestDto.LotEntryNumber();
			lotEntryNumber2.setEntryNo(2);
			plantingRequestDto.setLotPerEntryNo(Arrays.asList(lotEntryNumber1, lotEntryNumber2));
			Mockito
				.when(this.plantingService
					.searchPlantingPreparation(Mockito.eq(STUDY_ID), Mockito.eq(DATASET_ID), Mockito.any(SearchCompositeDto.class)))
				.thenReturn(plantingPreparationDTO);
			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, plantingRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.entry.no.invalid"));
		}
	}

	@Test
	public void validatePlantingRequestDto_InvalidLotPerEntryNo_ExceptionThrown() {
		try {
			final Integer entryNo = RandomUtils.nextInt();
			final Integer lotId = RandomUtils.nextInt();
			final PlantingPreparationDTO plantingPreparationDTO = new PlantingPreparationDTO();
			final PlantingPreparationDTO.PlantingPreparationEntryDTO entryDTO = new PlantingPreparationDTO.PlantingPreparationEntryDTO();
			entryDTO.setEntryNo(entryNo);
			plantingPreparationDTO.setEntries(Collections.singletonList(entryDTO));

			final PlantingRequestDto plantingRequestDto = new PlantingRequestDto();
			plantingRequestDto.setSelectedObservationUnits(new SearchCompositeDto<>());
			final PlantingRequestDto.WithdrawalInstruction withdrawalInstruction = new PlantingRequestDto.WithdrawalInstruction();
			withdrawalInstruction.setGroupTransactions(true);
			withdrawalInstruction.setWithdrawAllAvailableBalance(true);
			final Map<String, PlantingRequestDto.WithdrawalInstruction> withdrawalPerUnit = new HashMap<>();
			withdrawalPerUnit.put("Seed_amount_g", withdrawalInstruction);
			plantingRequestDto.setWithdrawalsPerUnit(withdrawalPerUnit);
			final PlantingRequestDto.LotEntryNumber lotEntryNumber1 = new PlantingRequestDto.LotEntryNumber();
			lotEntryNumber1.setEntryNo(entryNo);
			lotEntryNumber1.setLotId(lotId);
			plantingRequestDto.setLotPerEntryNo(Collections.singletonList(lotEntryNumber1));
			Mockito
				.when(this.plantingService
					.searchPlantingPreparation(Mockito.eq(STUDY_ID), Mockito.eq(DATASET_ID), Mockito.any(SearchCompositeDto.class)))
				.thenReturn(plantingPreparationDTO);
			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, plantingRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.lot.entry.no.invalid"));
		}
	}

	@Test
	public void validatePlantingRequestDto_MissingUnitInstructions_ExceptionThrown() {
		try {
			final Integer entryNo = RandomUtils.nextInt();
			final Integer unitId = RandomUtils.nextInt();
			final Integer lotId = RandomUtils.nextInt();
			final PlantingPreparationDTO plantingPreparationDTO = new PlantingPreparationDTO();
			final PlantingPreparationDTO.PlantingPreparationEntryDTO entryDTO = new PlantingPreparationDTO.PlantingPreparationEntryDTO();
			entryDTO.setEntryNo(entryNo);
			final Map<String, PlantingPreparationDTO.PlantingPreparationEntryDTO.StockDTO> stockDTOMap = new HashMap<>();
			final PlantingPreparationDTO.PlantingPreparationEntryDTO.StockDTO stockDTO =
				new PlantingPreparationDTO.PlantingPreparationEntryDTO.StockDTO();
			stockDTO.setLotId(lotId);
			stockDTO.setUnitId(unitId);
			stockDTOMap.put("STK1", stockDTO);
			entryDTO.setStockByStockId(stockDTOMap);
			plantingPreparationDTO.setEntries(Collections.singletonList(entryDTO));

			final PlantingRequestDto plantingRequestDto = new PlantingRequestDto();
			plantingRequestDto.setSelectedObservationUnits(new SearchCompositeDto<>());
			final PlantingRequestDto.WithdrawalInstruction withdrawalInstruction = new PlantingRequestDto.WithdrawalInstruction();
			withdrawalInstruction.setGroupTransactions(true);
			withdrawalInstruction.setWithdrawAllAvailableBalance(true);
			final Map<String, PlantingRequestDto.WithdrawalInstruction> withdrawalPerUnit = new HashMap<>();
			withdrawalPerUnit.put("Seed_amount_g", withdrawalInstruction);
			plantingRequestDto.setWithdrawalsPerUnit(withdrawalPerUnit);
			final PlantingRequestDto.LotEntryNumber lotEntryNumber1 = new PlantingRequestDto.LotEntryNumber();
			lotEntryNumber1.setEntryNo(entryNo);
			lotEntryNumber1.setLotId(lotId);
			plantingRequestDto.setLotPerEntryNo(Collections.singletonList(lotEntryNumber1));

			final VariableDetails variableDetails = new VariableDetails();
			variableDetails.setName("Seed_amount_kg");
			final VariableFilter variableFilter = new VariableFilter();
			variableFilter.addVariableIds(Collections.singletonList(unitId));
			Mockito.when(this.variableService.getVariablesByFilter(variableFilter)).thenReturn(Collections.singletonList(variableDetails));

			Mockito
				.when(this.plantingService
					.searchPlantingPreparation(Mockito.eq(STUDY_ID), Mockito.eq(DATASET_ID), Mockito.any(SearchCompositeDto.class)))
				.thenReturn(plantingPreparationDTO);

			this.plantingRequestDtoValidator.validatePlantingRequestDto(STUDY_ID, DATASET_ID, plantingRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("planting.missing.unit.instructions"));
		}
	}

}
