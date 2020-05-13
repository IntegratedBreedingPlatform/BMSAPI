package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotDepositRequestDtoValidator;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotDepositRequestDtoValidatorTest {

	@Mock
	private VariableService variableService;

	private List<VariableDetails> variableDetails;

	private VariableFilter variableFilter;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@InjectMocks
	private LotDepositRequestDtoValidator lotDepositRequestDtoValidator;

	@Before
	public void setUp() {
		variableDetails = this.buildVariableDetails();
		variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(variableService.getVariablesByFilter(variableFilter)).thenReturn(variableDetails);
		Mockito.doCallRealMethod().when(inventoryCommonValidator)
			.validateSearchCompositeDto(Mockito.any(SearchCompositeDto.class), Mockito.any(
				BindingResult.class));

	}

	@Test
	public void testValidateLotDepositRequestDtoValidatorIsNull() {
		try {
			this.lotDepositRequestDtoValidator.validate(null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.deposit.input.null"));

		}
	}

	@Test
	public void testValidateLotDepositRequestDtoValidatorLotsAreNotProperlySet() {
		try {
			final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
			final SearchCompositeDto<Integer> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			searchCompositeDto.setItemIds(new HashSet<>(Collections.singleton(1)));
			lotDepositRequestDto.setSelectedLots(searchCompositeDto);
			this.lotDepositRequestDtoValidator.validate(lotDepositRequestDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("search.composite.invalid"));
		}
	}

	@Test
	public void testValidateLotDepositRequestDtoValidatorInvalidNotes() {
		try {
			final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
			final SearchCompositeDto<Integer> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotDepositRequestDto.setSelectedLots(searchCompositeDto);
			lotDepositRequestDto.setNotes(RandomStringUtils.randomAlphabetic(256));
			this.lotDepositRequestDtoValidator.validate(lotDepositRequestDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.notes.length"));

		}
	}

	@Test
	public void testValidateLotDepositRequestDtoValidatorNullWithdrawalInstructions() {
		try {
			final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
			final SearchCompositeDto<Integer> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotDepositRequestDto.setSelectedLots(searchCompositeDto);
			lotDepositRequestDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			this.lotDepositRequestDtoValidator.validate(lotDepositRequestDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.deposit.instruction.invalid"));
		}
	}

	@Test
	public void testValidateLotDepositRequestDtoValidatorUnsupportedUnitName() {
		try {
			final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
			final SearchCompositeDto<Integer> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotDepositRequestDto.setSelectedLots(searchCompositeDto);
			lotDepositRequestDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final Map<String, Double> depositsPerUnit = new HashMap<>();
			depositsPerUnit.put("pounds", 20D);
			lotDepositRequestDto.setDepositsPerUnit(depositsPerUnit);
			this.lotDepositRequestDtoValidator.validate(lotDepositRequestDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.invalid.units"));
		}
	}

	@Test
	public void testValidateLotDepositRequestDtoValidatorInvalidAmount() {
		try {
			final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
			final SearchCompositeDto<Integer> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotDepositRequestDto.setSelectedLots(searchCompositeDto);
			lotDepositRequestDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final Map<String, Double> depositsPerUnit = new HashMap<>();
			depositsPerUnit.put("kg", 0D);
			lotDepositRequestDto.setDepositsPerUnit(depositsPerUnit);
			this.lotDepositRequestDtoValidator.validate(lotDepositRequestDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.amount.invalid"));
		}
	}

	@Test
	public void testValidateDepositInstructionsUnitsMissingUnits() {
		try {
			final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
			final SearchCompositeDto<Integer> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotDepositRequestDto.setSelectedLots(searchCompositeDto);
			lotDepositRequestDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final Map<String, Double> depositsPerUnit = new HashMap<>();
			depositsPerUnit.put("kg", 0D);
			lotDepositRequestDto.setDepositsPerUnit(depositsPerUnit);

			final List<ExtendedLotDto> extendedLotDtos = new ArrayList<>();
			final ExtendedLotDto extendedLotDto1 = new ExtendedLotDto();
			extendedLotDto1.setUnitName("kg");
			extendedLotDtos.add(extendedLotDto1);

			final ExtendedLotDto extendedLotDto2 = new ExtendedLotDto();
			extendedLotDto2.setUnitName("g");
			extendedLotDtos.add(extendedLotDto2);

			this.lotDepositRequestDtoValidator.validateDepositInstructionsUnits(lotDepositRequestDto, extendedLotDtos);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.instructions.missing.for.units"));
		}
	}

	@Test
	public void testValidateLotDepositRequestDtoValidatorExtraUnits() {
		try {
			final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
			final SearchCompositeDto<Integer> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotDepositRequestDto.setSelectedLots(searchCompositeDto);
			lotDepositRequestDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final Map<String, Double> depositsPerUnit = new HashMap<>();
			depositsPerUnit.put("kg", 1D);
			depositsPerUnit.put("g", 1D);
			lotDepositRequestDto.setDepositsPerUnit(depositsPerUnit);

			final List<ExtendedLotDto> extendedLotDtos = new ArrayList<>();
			final ExtendedLotDto extendedLotDto = new ExtendedLotDto();
			extendedLotDto.setUnitName("kg");
			extendedLotDtos.add(extendedLotDto);

			this.lotDepositRequestDtoValidator.validateDepositInstructionsUnits(lotDepositRequestDto, extendedLotDtos);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.instructions.for.non.present.units"));
		}
	}

	private List<VariableDetails> buildVariableDetails() {
		final VariableDetails unitKg = new VariableDetails();
		unitKg.setName("kg");
		final VariableDetails unitG = new VariableDetails();
		unitG.setName("g");
		final List<VariableDetails> variableDetails = Arrays.asList(unitKg, unitG);
		return variableDetails;
	}

}
