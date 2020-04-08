package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TransactionUpdateRequestDtoValidatorTest {

	@Mock
	private TransactionService transactionService;

	@InjectMocks
	private TransactionUpdateRequestDtoValidator transactionUpdateRequestDtoValidator;

	private List<TransactionDto> transactionDtos = new ArrayList<>();

	@Before
	public void setUp() {
		final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
		transactionsSearchDto.setTransactionIds(Arrays.asList(1, 2));
		Mockito.when(transactionService.searchTransactions(transactionsSearchDto, null)).thenReturn(transactionDtos);
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorIsNull() {
		try {
			this.transactionUpdateRequestDtoValidator.validate(null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.invalid.object"));

		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorIsEmpty() {
		try {
			this.transactionUpdateRequestDtoValidator.validate(new ArrayList<>());
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.invalid.size"));

		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorHasNullElement() {
		try {
			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(null);
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.invalid.transaction.update"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorHasInvalidData() {
		try {
			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, 2d, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.invalid.data"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorHasDuplicatedTransactionIds() {
		try {
			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, null, 2d, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.duplicated.ids"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorNotFoundTransactionIds() {
		try {
			this.transactionDtos.clear();
			final TransactionDto transaction = new TransactionDto();
			transaction.setTransactionId(1);
			this.transactionDtos.add(transaction);
			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, null, 2d, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transactions.not.found"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorClosedLots() {
		try {
			this.transactionDtos.clear();

			final TransactionDto transaction1 = new TransactionDto();
			transaction1.setTransactionId(1);
			final ExtendedLotDto lotDto1 = new ExtendedLotDto();
			lotDto1.setLotId(1);
			lotDto1.setStatus(LotStatus.CLOSED.name());
			transaction1.setLot(lotDto1);
			this.transactionDtos.add(transaction1);

			final TransactionDto transaction2 = new TransactionDto();
			transaction2.setTransactionId(1);
			final ExtendedLotDto lotDto2 = new ExtendedLotDto();
			lotDto2.setLotId(1);
			lotDto2.setStatus(LotStatus.ACTIVE.name());
			transaction2.setLot(lotDto2);
			this.transactionDtos.add(transaction2);

			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, null, 2d, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.closed.lots"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorNoPendingStatus() {
		try {
			this.transactionDtos.clear();

			final TransactionDto transaction1 = new TransactionDto();
			transaction1.setTransactionId(1);
			final ExtendedLotDto lotDto1 = new ExtendedLotDto();
			lotDto1.setLotId(1);
			lotDto1.setStatus(LotStatus.ACTIVE.name());
			transaction1.setLot(lotDto1);
			transaction1.setTransactionStatus(TransactionStatus.PENDING.getValue());
			this.transactionDtos.add(transaction1);

			final TransactionDto transaction2 = new TransactionDto();
			transaction2.setTransactionId(1);
			final ExtendedLotDto lotDto2 = new ExtendedLotDto();
			lotDto2.setLotId(1);
			lotDto2.setStatus(LotStatus.ACTIVE.name());
			transaction2.setLot(lotDto2);
			transaction2.setTransactionStatus(TransactionStatus.CONFIRMED.getValue());
			this.transactionDtos.add(transaction2);

			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, null, 2d, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.not.pending.status"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorWrongTransactionType() {
		try {
			this.transactionDtos.clear();

			final TransactionDto transaction1 = new TransactionDto();
			transaction1.setTransactionId(1);
			final ExtendedLotDto lotDto1 = new ExtendedLotDto();
			lotDto1.setLotId(1);
			lotDto1.setStatus(LotStatus.ACTIVE.name());
			transaction1.setLot(lotDto1);
			transaction1.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction1.setTransactionType(TransactionType.DEPOSIT.getValue());
			this.transactionDtos.add(transaction1);

			final TransactionDto transaction2 = new TransactionDto();
			transaction2.setTransactionId(1);
			final ExtendedLotDto lotDto2 = new ExtendedLotDto();
			lotDto2.setLotId(1);
			lotDto2.setStatus(LotStatus.ACTIVE.name());
			transaction2.setLot(lotDto2);
			transaction2.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction2.setTransactionType(TransactionType.ADJUSTMENT.getValue());

			this.transactionDtos.add(transaction2);

			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, null, 2d, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.wrong.type"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorInvalidNewAmount() {
		try {
			this.transactionDtos.clear();

			final TransactionDto transaction1 = new TransactionDto();
			transaction1.setTransactionId(1);
			final ExtendedLotDto lotDto1 = new ExtendedLotDto();
			lotDto1.setLotId(1);
			lotDto1.setStatus(LotStatus.ACTIVE.name());
			transaction1.setLot(lotDto1);
			transaction1.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction1.setTransactionType(TransactionType.DEPOSIT.getValue());
			this.transactionDtos.add(transaction1);

			final TransactionDto transaction2 = new TransactionDto();
			transaction2.setTransactionId(1);
			final ExtendedLotDto lotDto2 = new ExtendedLotDto();
			lotDto2.setLotId(1);
			lotDto2.setStatus(LotStatus.ACTIVE.name());
			transaction2.setLot(lotDto2);
			transaction2.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction2.setTransactionType(TransactionType.DEPOSIT.getValue());

			this.transactionDtos.add(transaction2);

			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, 0D,null, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.new.amount.error"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorInvalidNewBalance() {
		try {
			this.transactionDtos.clear();

			final TransactionDto transaction1 = new TransactionDto();
			transaction1.setTransactionId(1);
			final ExtendedLotDto lotDto1 = new ExtendedLotDto();
			lotDto1.setLotId(1);
			lotDto1.setStatus(LotStatus.ACTIVE.name());
			transaction1.setLot(lotDto1);
			transaction1.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction1.setTransactionType(TransactionType.DEPOSIT.getValue());
			this.transactionDtos.add(transaction1);

			final TransactionDto transaction2 = new TransactionDto();
			transaction2.setTransactionId(1);
			final ExtendedLotDto lotDto2 = new ExtendedLotDto();
			lotDto2.setLotId(1);
			lotDto2.setStatus(LotStatus.ACTIVE.name());
			transaction2.setLot(lotDto2);
			transaction2.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction2.setTransactionType(TransactionType.DEPOSIT.getValue());

			this.transactionDtos.add(transaction2);

			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, 1D,null, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, -2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.new.balance.error"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorUpdateNewBalanceForSameLot() {
		try {
			this.transactionDtos.clear();

			final TransactionDto transaction1 = new TransactionDto();
			transaction1.setTransactionId(1);
			final ExtendedLotDto lotDto1 = new ExtendedLotDto();
			lotDto1.setLotId(1);
			lotDto1.setStatus(LotStatus.ACTIVE.name());
			transaction1.setLot(lotDto1);
			transaction1.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction1.setTransactionType(TransactionType.DEPOSIT.getValue());
			this.transactionDtos.add(transaction1);

			final TransactionDto transaction2 = new TransactionDto();
			transaction2.setTransactionId(1);
			transaction2.setLot(lotDto1);
			transaction2.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction2.setTransactionType(TransactionType.DEPOSIT.getValue());

			this.transactionDtos.add(transaction2);

			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, null,2d, ""));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.new.balance.for.same.lot"));
		}
	}

	@Test
	public void testValidateTransactionUpdateRequestDtoValidatorInvalidNotes() {
		try {
			this.transactionDtos.clear();

			final TransactionDto transaction1 = new TransactionDto();
			transaction1.setTransactionId(1);
			final ExtendedLotDto lotDto1 = new ExtendedLotDto();
			lotDto1.setLotId(1);
			lotDto1.setStatus(LotStatus.ACTIVE.name());
			transaction1.setLot(lotDto1);
			transaction1.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction1.setTransactionType(TransactionType.DEPOSIT.getValue());
			this.transactionDtos.add(transaction1);

			final TransactionDto transaction2 = new TransactionDto();
			transaction2.setTransactionId(1);
			final ExtendedLotDto lotDto2 = new ExtendedLotDto();
			lotDto2.setLotId(1);
			lotDto2.setStatus(LotStatus.ACTIVE.name());
			transaction2.setLot(lotDto2);
			transaction2.setTransactionStatus(TransactionStatus.PENDING.getValue());
			transaction2.setTransactionType(TransactionType.DEPOSIT.getValue());

			this.transactionDtos.add(transaction2);

			final List<TransactionUpdateRequestDto> transactionUpdateRequestDtoList = new ArrayList<>();
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(1, 1D,null, RandomStringUtils.randomAlphabetic(256)));
			transactionUpdateRequestDtoList.add(new TransactionUpdateRequestDto(2, null, 2d, ""));
			this.transactionUpdateRequestDtoValidator.validate(transactionUpdateRequestDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.update.invalid.notes.length"));
		}
	}

}