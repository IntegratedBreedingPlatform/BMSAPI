package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotWithdrawalInputDtoValidator;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotWithdrawalInputDtoValidatorTest {

	@Mock
	private VariableService variableService;

	@InjectMocks
	private LotWithdrawalInputDtoValidator lotWithdrawalInputDtoValidator;

	private List<VariableDetails> variableDetails;

	private VariableFilter variableFilter;

	@Before
	public void setUp() {
		variableDetails = this.buildVariableDetails();
		variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(variableService.getVariablesByFilter(variableFilter)).thenReturn(variableDetails);
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorIsNull() {
		try {
			this.lotWithdrawalInputDtoValidator.validate(null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.withdrawal.input.null"));

		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorLotsAreNotProperlySet() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			lotWithdrawalInputDto.setLotsSearchId(1);
			lotWithdrawalInputDto.setLotIds(new HashSet<>(Arrays.asList(1)));
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.selection.invalid"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorInvalidNotes() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			lotWithdrawalInputDto.setLotsSearchId(1);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(256));
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.notes.length"));

		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorNullWithdrawalInstructions() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			lotWithdrawalInputDto.setLotsSearchId(1);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.withdrawal.input.null"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorUnsupportedUnitName() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			lotWithdrawalInputDto.setLotsSearchId(1);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final LotWithdrawalInputDto.WithdrawalAmountInstruction withdrawalAmountInstruction =
				new LotWithdrawalInputDto.WithdrawalAmountInstruction();
			final Map<String, LotWithdrawalInputDto.WithdrawalAmountInstruction> withdrawalsPerUnit = new HashMap<>();
			withdrawalsPerUnit.put("pounds", withdrawalAmountInstruction);
			lotWithdrawalInputDto.setWithdrawalsPerUnit(withdrawalsPerUnit);
			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.invalid.units"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorInvalidAmount() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			lotWithdrawalInputDto.setLotsSearchId(1);
			lotWithdrawalInputDto.setNotes(RandomStringUtils.randomAlphabetic(255));
			final LotWithdrawalInputDto.WithdrawalAmountInstruction withdrawalAmountInstruction =
				new LotWithdrawalInputDto.WithdrawalAmountInstruction();
			final Map<String, LotWithdrawalInputDto.WithdrawalAmountInstruction> withdrawalsPerUnit = new HashMap<>();
			withdrawalsPerUnit.put("kg", withdrawalAmountInstruction);
			lotWithdrawalInputDto.setWithdrawalsPerUnit(withdrawalsPerUnit);

			final ExtendedLotDto extendedLotDto1 = new ExtendedLotDto();
			extendedLotDto1.setUnitName("kg");

			this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.amount.invalid"));
		}
	}

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorMissingWithdrawalsInstructionsForUnit() {
		try {
			final LotWithdrawalInputDto lotWithdrawalInputDto = new LotWithdrawalInputDto();
			lotWithdrawalInputDto.setLotsSearchId(1);
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
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.instructions.missing.for.units"));
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
