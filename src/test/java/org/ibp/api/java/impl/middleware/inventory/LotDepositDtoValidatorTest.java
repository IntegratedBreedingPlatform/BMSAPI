package org.ibp.api.java.impl.middleware.inventory;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.LotDepositDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotDepositDtoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotDepositDtoValidatorTest {

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@InjectMocks
	private LotDepositDtoValidator lotDepositDtoValidator;

	@Before
	public void setUp() {
	}

	@Test
	public void testValidateLotDepositDtoValidatorIsNull() {
		try {
			this.lotDepositDtoValidator.validate(null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.deposit.input.null"));

		}
	}

	@Test
	public void testValidateLotDepositDtoValidatorInvalidNotes() {
		try {
			final LotDepositDto lotDepositDto = new LotDepositDto();
			lotDepositDto.setNotes(RandomStringUtils.randomAlphabetic(256));
			lotDepositDto.setAmount(10D);
			this.lotDepositDtoValidator.validate(ImmutableList.<LotDepositDto>of(lotDepositDto));
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.notes.length"));

		}
	}

	@Test
	public void testValidateLotDepositDtoValidatorInvalidAmount() {
		try {
			final LotDepositDto lotDepositDto = new LotDepositDto();
			lotDepositDto.setNotes(RandomStringUtils.randomAlphabetic(10));
			lotDepositDto.setAmount(-10D);
			this.lotDepositDtoValidator.validate(ImmutableList.<LotDepositDto>of(lotDepositDto));
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.amount.invalid"));

		}
	}
}

