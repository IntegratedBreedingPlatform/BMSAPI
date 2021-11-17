package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotWithdrawalInputDtoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotWithdrawalInputDtoValidatorTest {

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Mock
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@InjectMocks
	private LotWithdrawalInputDtoValidator lotWithdrawalInputDtoValidator;

	@Before
	public void setUp() {
		Mockito.doCallRealMethod().when(this.searchCompositeDtoValidator)
			.validateSearchCompositeDto(Mockito.any(SearchCompositeDto.class), Mockito.any(BindingResult.class));
		Mockito.doCallRealMethod().when(this.inventoryCommonValidator)
			.validateTransactionNotes(Mockito.anyString(), Mockito.any(BindingResult.class));
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorIsNull() {
		try {
			this.lotWithdrawalInputDtoValidator.validate(null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.withdrawal.input.null"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorLotsAreNotProperlySet() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			final SearchCompositeDto<Integer, String> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			searchCompositeDto.setItemIds(Collections.singletonList(RandomStringUtils.randomAlphabetic(38)));
			lotWithdrawalInputDto.setSelectedLots(searchCompositeDto);
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("search.composite.invalid"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorInvalidNotes() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			final SearchCompositeDto<Integer, String> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotWithdrawalInputDto.setSelectedLots(searchCompositeDto);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(256));
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.notes.length"));

		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorNullWithdrawalInstructions() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			final SearchCompositeDto<Integer, String> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotWithdrawalInputDto.setSelectedLots(searchCompositeDto);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.withdrawal.input.null"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorUnsupportedUnitName() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			final SearchCompositeDto<Integer, String> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotWithdrawalInputDto.setSelectedLots(searchCompositeDto);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final LotWithdrawalInputDto.WithdrawalAmountInstruction withdrawalAmountInstruction =
				new LotWithdrawalInputDto.WithdrawalAmountInstruction();
			final Map<String, LotWithdrawalInputDto.WithdrawalAmountInstruction> withdrawalsPerUnit = new HashMap<>();
			withdrawalsPerUnit.put("kg", withdrawalAmountInstruction);
			lotWithdrawalInputDto.setWithdrawalsPerUnit(withdrawalsPerUnit);
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
			errors.reject("lot.input.invalid.units", "");
			Mockito.doThrow(new ApiRequestValidationException(errors.getAllErrors())).when(this.inventoryCommonValidator)
				.validateUnitNames(Mockito.any(List.class), Mockito.any(BindingResult.class));
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.invalid.units"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorInvalidAmount() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			final SearchCompositeDto<Integer, String> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotWithdrawalInputDto.setSelectedLots(searchCompositeDto);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final LotWithdrawalInputDto.WithdrawalAmountInstruction withdrawalAmountInstruction =
				new LotWithdrawalInputDto.WithdrawalAmountInstruction();
			final Map<String, LotWithdrawalInputDto.WithdrawalAmountInstruction> withdrawalsPerUnit = new HashMap<>();
			withdrawalsPerUnit.put("kg", withdrawalAmountInstruction);
			lotWithdrawalInputDto.setWithdrawalsPerUnit(withdrawalsPerUnit);

			final ExtendedLotDto extendedLotDto1 = new ExtendedLotDto();
			extendedLotDto1.setUnitName("kg");

			Mockito.doNothing().when(this.inventoryCommonValidator)
				.validateUnitNames(Mockito.any(List.class), Mockito.any(BindingResult.class));
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.amount.invalid"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorMissingWithdrawalsInstructionsForUnit() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			final SearchCompositeDto<Integer, String> searchCompositeDto = new SearchCompositeDto();
			searchCompositeDto.setSearchRequest(1);
			lotWithdrawalInputDto.setSelectedLots(searchCompositeDto);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final LotWithdrawalInputDto.WithdrawalAmountInstruction withdrawalAmountInstruction =
				new LotWithdrawalInputDto.WithdrawalAmountInstruction();
			final Map<String, LotWithdrawalInputDto.WithdrawalAmountInstruction> withdrawalsPerUnit = new HashMap<>();
			withdrawalsPerUnit.put("kg", withdrawalAmountInstruction);
			lotWithdrawalInputDto.setWithdrawalsPerUnit(withdrawalsPerUnit);

			final ExtendedLotDto extendedLotDto1 = new ExtendedLotDto();
			extendedLotDto1.setUnitName("kg");
			final ExtendedLotDto extendedLotDto2 = new ExtendedLotDto();
			extendedLotDto2.setUnitName("g");

			final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(extendedLotDto1, extendedLotDto2);

			this.lotWithdrawalInputDtoValidator.validateWithdrawalInstructionsUnits(lotWithdrawalInputDto, extendedLotDtos);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.instructions.missing.for.units"));
		}
	}

}
