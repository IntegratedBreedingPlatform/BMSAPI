package org.ibp.api.java.impl.middleware.inventory;

import factory.ExtendedLotDtoDummyFactory;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotSplitValidatorTest {

	private static final String SPLIT_LOT_UUID = UUID.randomUUID().toString();

	@InjectMocks
	private LotSplitValidator lotSplitValidator;

	@Mock
	private LotInputValidator lotInputValidator;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldRequestBeValid() {
		final LotSplitRequestDto lotSplitRequestDto = this.createDummyLotSplitRequestDto();

		this.lotSplitValidator.validateRequest(lotSplitRequestDto);
	}

	@Test
	public void shouldFailValidateRequestWithNullRequest() {
		try {
			this.lotSplitValidator.validateRequest(null);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.input.null"));
		}
	}

	@Test
	public void shouldFailValidateRequestWithNullSplitLotUUID() {
		try {
			this.lotSplitValidator.validateRequest(new LotSplitRequestDto());
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.uuid.null"));
		}
	}

	@Test
	public void shouldFailValidateRequestWithEmptySplitLotUUID() {
		try {
			this.lotSplitValidator.validateRequest(this.createDummyLotSplitRequestDto(""));
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.uuid.null"));
		}
	}

	@Test
	public void shouldFailValidateRequestWithNullInitialDeposit() {
		try {
			final LotSplitRequestDto dummyLotSplitRequestDto = this.createDummyLotSplitRequestDto();
			dummyLotSplitRequestDto.setInitialDeposit(null);
			this.lotSplitValidator.validateRequest(dummyLotSplitRequestDto);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.initial.deposit.null"));
		}
	}

	@Test
	public void shouldFailValidateRequestWithNullINewLot() {
		try {
			final LotSplitRequestDto dummyLotSplitRequestDto = this.createDummyLotSplitRequestDto();
			dummyLotSplitRequestDto.setNewLot(null);
			this.lotSplitValidator.validateRequest(dummyLotSplitRequestDto);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.new.lot.null"));
		}
	}

	@Test
	public void shouldValidateSplitLot() {
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create();

		Mockito.doNothing().when(this.lotInputValidator).validateLotBalance(lotDto.getActualBalance());

		this.lotSplitValidator.validateSplitLot(lotDto, 2D);

		Mockito.verify(this.lotInputValidator).validateLotBalance(lotDto.getActualBalance());
	}

	@Test
	public void shouldFailValidateSplitLotWithInitialDepositEqualToSplitBalance() {
		final double amount = 5D;
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(amount);

		Mockito.doNothing().when(this.lotInputValidator).validateLotBalance(lotDto.getActualBalance());

		try {
			this.lotSplitValidator.validateSplitLot(lotDto, amount);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.new.lot.invalid.amount"));
		}

		Mockito.verify(this.lotInputValidator).validateLotBalance(lotDto.getActualBalance());
	}

	@Test
	public void shouldFailValidateSplitLotWithInitialDepositGreaterThanSplitBalance() {
		final double amount = 5D;
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(amount);

		Mockito.doNothing().when(this.lotInputValidator).validateLotBalance(lotDto.getActualBalance());

		try {
			this.lotSplitValidator.validateSplitLot(lotDto, amount + 1);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.new.lot.invalid.amount"));
		}

		Mockito.verify(this.lotInputValidator).validateLotBalance(lotDto.getActualBalance());
	}

	@Test
	public void shouldFailValidateSplitLotWithNotActiveSplitLot() {
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(1, LotStatus.CLOSED, "unitName");

		try {
			this.lotSplitValidator.validateSplitLot(lotDto, 1D);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lots.closed"));
		}

		Mockito.verifyZeroInteractions(this.lotInputValidator);
	}

	private LotSplitRequestDto createDummyLotSplitRequestDto() {
		return this.createDummyLotSplitRequestDto(SPLIT_LOT_UUID);
	}

	private LotSplitRequestDto createDummyLotSplitRequestDto(final String splitLotUUID) {
		final LotSplitRequestDto lotSplitRequestDto = new LotSplitRequestDto();
		lotSplitRequestDto.setSplitLotUUID(splitLotUUID);

		final LotSplitRequestDto.InitialLotDepositDto initialLotDepositDto = new LotSplitRequestDto.InitialLotDepositDto();
		initialLotDepositDto.setAmount(5d);
		lotSplitRequestDto.setInitialDeposit(initialLotDepositDto);

		final LotSplitRequestDto.NewLotSplitDto newLotSplitDto = new LotSplitRequestDto.NewLotSplitDto();
		lotSplitRequestDto.setNewLot(newLotSplitDto);

		return lotSplitRequestDto;
	}
}
