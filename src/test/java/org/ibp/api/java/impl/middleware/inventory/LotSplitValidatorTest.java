package org.ibp.api.java.impl.middleware.inventory;

import factory.ExtendedLotDtoDummyFactory;
import gherkin.formatter.Argument;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotSplitValidatorTest {

	private static final String SPLIT_LOT_UUID = UUID.randomUUID().toString();
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	@InjectMocks
	private LotSplitValidator lotSplitValidator;

	@Mock
	private LotInputValidator lotInputValidator;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Mock
	private LocationValidator locationValidator;

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
		final LotSplitRequestDto.NewLotSplitDto dummyNewLotSplitDto = this.createDummyNewLotSplitDto();
		final LotSplitRequestDto.InitialLotDepositDto dummyInitialLotDepositDto = this.createDummyInitialLotDepositDto(5d);

		Mockito.doNothing().when(this.inventoryCommonValidator).validateLotNotes(ArgumentMatchers.eq(dummyNewLotSplitDto.getNotes()), ArgumentMatchers.any(
			BindingResult.class));
		Mockito.doNothing().when(this.inventoryCommonValidator).validateTransactionNotes(ArgumentMatchers.eq(dummyInitialLotDepositDto.getNotes()), ArgumentMatchers.any(
			BindingResult.class));
		Mockito.doNothing().when(this.locationValidator).validateSeedLocationId(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(PROGRAM_UUID), ArgumentMatchers.eq(dummyNewLotSplitDto.getLocationId()));

		this.lotSplitValidator.validateSplitLot(PROGRAM_UUID, lotDto, dummyNewLotSplitDto, dummyInitialLotDepositDto);

		Mockito.verify(this.inventoryCommonValidator).validateLotNotes(ArgumentMatchers.eq(dummyNewLotSplitDto.getNotes()), ArgumentMatchers.any(
			BindingResult.class));
		Mockito.verify(this.inventoryCommonValidator).validateTransactionNotes(ArgumentMatchers.eq(dummyInitialLotDepositDto.getNotes()), ArgumentMatchers.any(
			BindingResult.class));
		Mockito.verify(this.locationValidator).validateSeedLocationId(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(PROGRAM_UUID), ArgumentMatchers.eq(dummyNewLotSplitDto.getLocationId()));
	}

	@Test
	public void shouldFailValidateSplitLotWithInitialDepositEqualToSplitBalance() {
		final double amount = 5D;
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(amount);

		final LotSplitRequestDto dummyLotSplitRequestDto = this.createDummyLotSplitRequestDto();
		dummyLotSplitRequestDto.getInitialDeposit().setAmount(amount);

		try {
			this.lotSplitValidator.validateSplitLot(PROGRAM_UUID, lotDto, Mockito.mock(LotSplitRequestDto.NewLotSplitDto.class),
				dummyLotSplitRequestDto.getInitialDeposit());
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.new.lot.invalid.amount"));
		}

		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.locationValidator);
	}

	@Test
	public void shouldFailValidateSplitLotWithInitialDepositGreaterThanSplitBalance() {
		final double amount = 5D;
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(amount);

		final LotSplitRequestDto dummyLotSplitRequestDto = this.createDummyLotSplitRequestDto();
		dummyLotSplitRequestDto.getInitialDeposit().setAmount(amount + 1);

		try {
			this.lotSplitValidator.validateSplitLot(PROGRAM_UUID, lotDto, Mockito.mock(LotSplitRequestDto.NewLotSplitDto.class),
				dummyLotSplitRequestDto.getInitialDeposit());
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.new.lot.invalid.amount"));
		}

		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.locationValidator);
	}

	@Test
	public void shouldFailValidateSplitLotWithNotActiveSplitLot() {
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(1, LotStatus.CLOSED, "unitName");

		try {
			this.lotSplitValidator.validateSplitLot(PROGRAM_UUID, lotDto,
				Mockito.mock(LotSplitRequestDto.NewLotSplitDto.class),
				Mockito.mock(LotSplitRequestDto.InitialLotDepositDto.class));
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lots.closed"));
		}

		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.locationValidator);
	}

	@Test
	public void shouldFailValidateSplitLotWithZeroAvailableBalance() {
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(1, LotStatus.ACTIVE, "unitName");
		lotDto.setAvailableBalance(0D);

		try {
			this.lotSplitValidator.validateSplitLot(PROGRAM_UUID, lotDto,
				Mockito.mock(LotSplitRequestDto.NewLotSplitDto.class),
				Mockito.mock(LotSplitRequestDto.InitialLotDepositDto.class));
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.new.lot.invalid.available.balance"));
		}

		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.locationValidator);
	}

	@Test
	public void shouldFailValidateSplitLotWithZeroInitialDepositAmount() {
		final ExtendedLotDto lotDto = ExtendedLotDtoDummyFactory.create(1, LotStatus.ACTIVE, "unitName");
		lotDto.setAvailableBalance(5D);

		LotSplitRequestDto.InitialLotDepositDto dummyInitialLotDepositDto = this.createDummyInitialLotDepositDto(0D);

		try {
			this.lotSplitValidator.validateSplitLot(PROGRAM_UUID, lotDto,
				Mockito.mock(LotSplitRequestDto.NewLotSplitDto.class),
				dummyInitialLotDepositDto);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.split.initial.deposit.invalid"));
		}

		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.locationValidator);
	}

	private LotSplitRequestDto createDummyLotSplitRequestDto() {
		return this.createDummyLotSplitRequestDto(SPLIT_LOT_UUID);
	}

	private LotSplitRequestDto createDummyLotSplitRequestDto(final String splitLotUUID) {
		final LotSplitRequestDto lotSplitRequestDto = new LotSplitRequestDto();
		lotSplitRequestDto.setSplitLotUUID(splitLotUUID);

		final LotSplitRequestDto.InitialLotDepositDto initialLotDepositDto = this.createDummyInitialLotDepositDto(5d);
		lotSplitRequestDto.setInitialDeposit(initialLotDepositDto);

		final LotSplitRequestDto.NewLotSplitDto newLotSplitDto = this.createDummyNewLotSplitDto();
		lotSplitRequestDto.setNewLot(newLotSplitDto);

		return lotSplitRequestDto;
	}

	private LotSplitRequestDto.InitialLotDepositDto createDummyInitialLotDepositDto(double amount) {
		final LotSplitRequestDto.InitialLotDepositDto initialLotDepositDto = new LotSplitRequestDto.InitialLotDepositDto();
		initialLotDepositDto.setAmount(amount);
		initialLotDepositDto.setNotes("Initial Deposit note");
		return initialLotDepositDto;
	}

	private LotSplitRequestDto.NewLotSplitDto createDummyNewLotSplitDto() {
		final LotSplitRequestDto.NewLotSplitDto newLotSplitDto = new LotSplitRequestDto.NewLotSplitDto();
		newLotSplitDto.setNotes("New Lot note");
		newLotSplitDto.setLocationId(new Random().nextInt());
		return newLotSplitDto;
	}
}
