package org.ibp.api.java.impl.middleware.inventory;

import liquibase.util.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotMultiUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSingleUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.InventoryUnitValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotInputValidatorTest {

	public static final int GID = 1;
	public static final int LOCATION_ID = 6000;
	public static final int UNIT_ID = TermId.SEED_AMOUNT_G.getId();
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";
	public static final String STOCK_PREFIX = "123";

	@InjectMocks
	private LotInputValidator lotInputValidator;

	@Mock
	private LocationValidator locationValidator;

	@Mock
	private InventoryUnitValidator inventoryUnitValidator;

	@Mock
	private ExtendedLotListValidator extendedLotListValidator;

	@Mock
	private LotService lotService;

	@Mock
	private TransactionService transactionService;

	private LotGeneratorInputDto lotGeneratorInputDto;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Captor
	private ArgumentCaptor<LotsSearchDto> lotsSearchDtoArgumentCaptor;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataComments() {
		Mockito.doCallRealMethod().when(this.inventoryCommonValidator).validateLotNotes(Mockito.anyString(), Mockito.any(BindingResult.class));
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(RandomStringUtils.randomAlphabetic(256));
		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockNull() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrue() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrueWithPrefix() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setStockPrefix(STOCK_PREFIX);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrueWithInvalidPrefix() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setStockPrefix(RandomStringUtils.randomAlphabetic(20));
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockFalse() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setStockPrefix(STOCK_PREFIX);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockFalseWithInvalidStock() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setStockId(RandomStringUtils.randomAlphabetic(40));

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidUnitIdAndNameConfirm() {
		final ExtendedLotDto extendedLotDto = this.getExtendedlotDto("SEED");
		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		lotUpdateRequestDto.setSingleInput(this.getLotSingleUpdateRequestDto());
		Mockito.when(this.transactionService.searchTransactions(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(this.getMockedTransactionsSearchDTO(extendedLotDto));
		this.lotInputValidator.validate(null, Arrays.asList(extendedLotDto), lotUpdateRequestDto);
	}

	@Test
	public void testInValidateUnitIdAndNameConfirm() {
		boolean pass = true;
		final ExtendedLotDto extendedLotDto = this.getExtendedlotDto("");
		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		lotUpdateRequestDto.setSingleInput(this.getLotSingleUpdateRequestDto());
		Mockito.when(this.transactionService.searchTransactions(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(this.getMockedTransactionsSearchDTO(extendedLotDto));
		try {
			this.lotInputValidator.validate(null, Arrays.asList(extendedLotDto), lotUpdateRequestDto);
		} catch (final Exception e) {
			pass = false;
		}
		Assert.assertTrue("No exception, existing unit id is not valid and can be updated.", pass);
	}

	@Test
	public void testValidate_ValidNewLotUIDs_OK() {
		final String newLotUID = UUID.randomUUID().toString();
		final List<ExtendedLotDto> lotDtos = Arrays.asList(this.getExtendedlotDto(""));

		Mockito.doNothing().when(this.extendedLotListValidator).validateClosedLots(lotDtos);

		Mockito.when(this.lotService.searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.isNull())).thenReturn(new ArrayList<>());

		final LotMultiUpdateRequestDto lotMultiUpdateRequestDto = new LotMultiUpdateRequestDto();
		lotMultiUpdateRequestDto.setLotList(Arrays.asList(this.createDummyLotUpdateDto(newLotUID)));

		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		lotUpdateRequestDto.setMultiInput(lotMultiUpdateRequestDto);

		this.lotInputValidator.validate(null, lotDtos, lotUpdateRequestDto);

		this.verifySearchLots(newLotUID);

		Mockito.verifyZeroInteractions(this.locationValidator);
		Mockito.verifyZeroInteractions(this.inventoryUnitValidator);
		Mockito.verifyZeroInteractions(this.germplasmValidator);
		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.transactionService);
	}

	@Test
	public void testValidate_DuplicatedNewLotUIDs_FAIL() {
		final List<ExtendedLotDto> lotDtos = Arrays.asList(this.getExtendedlotDto(""));

		Mockito.doNothing().when(this.extendedLotListValidator).validateClosedLots(lotDtos);

		Mockito.when(this.lotService.searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.isNull())).thenReturn(new ArrayList<>());

		final String newLotUID = UUID.randomUUID().toString();
		final LotMultiUpdateRequestDto.LotUpdateDto dummyLotUpdateDto = this.createDummyLotUpdateDto(newLotUID);
		final LotMultiUpdateRequestDto.LotUpdateDto duplicatedDummyLotUpdateDto = this.createDummyLotUpdateDto(newLotUID);

		final LotMultiUpdateRequestDto lotMultiUpdateRequestDto = new LotMultiUpdateRequestDto();
		lotMultiUpdateRequestDto.setLotList(Arrays.asList(dummyLotUpdateDto, duplicatedDummyLotUpdateDto));

		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		lotUpdateRequestDto.setMultiInput(lotMultiUpdateRequestDto);

		try {
			this.lotInputValidator.validate(null, lotDtos, lotUpdateRequestDto);
			Assert.fail("Should has failed");
		} catch (Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			final ApiRequestValidationException exception = (ApiRequestValidationException) e;
			assertThat(exception.getErrors(), hasSize(1));

			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getCodes()), hasItem("lot.update.duplicated.new.lot.uids"));
			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getArguments()), hasSize(1));
			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getArguments()[0]), hasItem("[" + newLotUID + "]"));
		}

		this.verifySearchLots(newLotUID);

		Mockito.verifyZeroInteractions(this.locationValidator);
		Mockito.verifyZeroInteractions(this.inventoryUnitValidator);
		Mockito.verifyZeroInteractions(this.germplasmValidator);
		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.transactionService);
	}

	@Test
	public void testValidate_InvalidNewLotUIDs_FAIL() {
		final List<ExtendedLotDto> lotDtos = Arrays.asList(this.getExtendedlotDto(""));

		Mockito.doNothing().when(this.extendedLotListValidator).validateClosedLots(lotDtos);

		Mockito.when(this.lotService.searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.isNull())).thenReturn(new ArrayList<>());

		final String invalidNewLotUID = StringUtils.repeat("0", LotInputValidator.NEW_LOT_UID_MAX_LENGTH + 1);
		final LotMultiUpdateRequestDto.LotUpdateDto invalidLotUpdateDto = this.createDummyLotUpdateDto(invalidNewLotUID);

		final LotMultiUpdateRequestDto lotMultiUpdateRequestDto = new LotMultiUpdateRequestDto();
		lotMultiUpdateRequestDto.setLotList(Arrays.asList(invalidLotUpdateDto));

		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		lotUpdateRequestDto.setMultiInput(lotMultiUpdateRequestDto);

		try {
			this.lotInputValidator.validate(null, lotDtos, lotUpdateRequestDto);
			Assert.fail("Should has failed");
		} catch (Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			final ApiRequestValidationException exception = (ApiRequestValidationException) e;
			assertThat(exception.getErrors(), hasSize(1));

			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getCodes()), hasItem("lot.update.invalid.new.lot.uids"));
			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getArguments()), hasSize(2));
			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getArguments()[0]), hasItem("[" + invalidNewLotUID + "]"));
			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getArguments()[1]), hasItem(String.valueOf(LotInputValidator.NEW_LOT_UID_MAX_LENGTH)));
		}

		this.verifySearchLots(invalidNewLotUID);

		Mockito.verifyZeroInteractions(this.locationValidator);
		Mockito.verifyZeroInteractions(this.inventoryUnitValidator);
		Mockito.verifyZeroInteractions(this.germplasmValidator);
		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.transactionService);
		Mockito.verifyZeroInteractions(this.lotService);
	}

	@Test
	public void testValidate_ExistingLotsForGivenNewLotUIDs_FAIL() {
		final String newLotUID = UUID.randomUUID().toString();
		final List<ExtendedLotDto> lotDtos = Arrays.asList(this.getExtendedlotDto(""));

		Mockito.doNothing().when(this.extendedLotListValidator).validateClosedLots(lotDtos);

		final ExtendedLotDto extendedLotDto = Mockito.mock(ExtendedLotDto.class);
		Mockito.when(extendedLotDto.getLotUUID()).thenReturn(newLotUID);
		Mockito.when(this.lotService.searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.isNull()))
			.thenReturn(Arrays.asList(extendedLotDto));

		final LotMultiUpdateRequestDto lotMultiUpdateRequestDto = new LotMultiUpdateRequestDto();
		lotMultiUpdateRequestDto.setLotList(Arrays.asList(this.createDummyLotUpdateDto(newLotUID)));

		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		lotUpdateRequestDto.setMultiInput(lotMultiUpdateRequestDto);

		try {
			this.lotInputValidator.validate(null, lotDtos, lotUpdateRequestDto);
			Assert.fail("Should has failed");
		} catch (Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			final ApiRequestValidationException exception = (ApiRequestValidationException) e;
			assertThat(exception.getErrors(), hasSize(1));

			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getCodes()), hasItem("lot.update.existing.new.lot.uids"));
			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getArguments()), hasSize(1));
			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getArguments()[0]), hasItem(newLotUID));
		}

		this.verifySearchLots(newLotUID);

		Mockito.verifyZeroInteractions(this.locationValidator);
		Mockito.verifyZeroInteractions(this.inventoryUnitValidator);
		Mockito.verifyZeroInteractions(this.germplasmValidator);
		Mockito.verifyZeroInteractions(this.inventoryCommonValidator);
		Mockito.verifyZeroInteractions(this.transactionService);
	}

	private ExtendedLotDto getExtendedlotDto(final String unitname) {
		final int unitId = unitname == null ? 0 : UNIT_ID;
		final ExtendedLotDto extendedLotDto = new ExtendedLotDto();
		extendedLotDto.setActualBalance(100.0);
		extendedLotDto.setAvailableBalance(100.0);
		extendedLotDto.setCreatedByUsername("admin");
		extendedLotDto.setCreatedDate(new Date());
		extendedLotDto.setDesignation("SAMPLE");
		extendedLotDto.setGermplasmLocation(String.valueOf(LOCATION_ID));
		extendedLotDto.setGermplasmMethodName("Derivative");
		extendedLotDto.setUnitName(unitname);
		extendedLotDto.setGid(GID);
		extendedLotDto.setUnitId(unitId);
		extendedLotDto.setLocationName("SAMPLE");
		extendedLotDto.setLotId(1);
		extendedLotDto.setStockId(STOCK_PREFIX+STOCK_ID);
		extendedLotDto.setNotes(COMMENTS);
		return extendedLotDto;
	}

	private LotSingleUpdateRequestDto getLotSingleUpdateRequestDto() {
		final LotSingleUpdateRequestDto lotSingleUpdateRequestDto = new LotSingleUpdateRequestDto();
		lotSingleUpdateRequestDto.setGid(GID);
		lotSingleUpdateRequestDto.setLocationId(LOCATION_ID);
		lotSingleUpdateRequestDto.setNotes(COMMENTS);
		lotSingleUpdateRequestDto.setUnitId(UNIT_ID);
		return lotSingleUpdateRequestDto;
	}

	private List<TransactionDto> getMockedTransactionsSearchDTO(final ExtendedLotDto extendedLotDto) {
		final TransactionDto transactionDto = new TransactionDto();
		transactionDto.setAmount(100.0);
		transactionDto.setLot(extendedLotDto);
		transactionDto.setTransactionId(1);
		transactionDto.setTransactionStatus(TransactionStatus.CONFIRMED.getValue());
		transactionDto.setNotes(COMMENTS);
		return Arrays.asList(transactionDto);
	}

	private LotMultiUpdateRequestDto.LotUpdateDto createDummyLotUpdateDto(final String newLotUID) {
		final LotMultiUpdateRequestDto.LotUpdateDto lotUpdateDto = new LotMultiUpdateRequestDto.LotUpdateDto();
		lotUpdateDto.setLotUID(UUID.randomUUID().toString());
		lotUpdateDto.setNewLotUID(newLotUID);
		return lotUpdateDto;
	}

	private void verifySearchLots(final String givenLotUID) {
		Mockito.verify(this.lotService).searchLots(this.lotsSearchDtoArgumentCaptor.capture(), ArgumentMatchers.isNull());
		final LotsSearchDto actualLotsSearchDto = this.lotsSearchDtoArgumentCaptor.getValue();
		assertNotNull(actualLotsSearchDto);
		final List<String> actualLotUUIDs = actualLotsSearchDto.getLotUUIDs();
		assertFalse(CollectionUtils.isEmpty(actualLotUUIDs));
		assertThat(actualLotUUIDs, hasSize(1));
		assertThat(actualLotUUIDs.get(0), is(givenLotUID));
	}


}
