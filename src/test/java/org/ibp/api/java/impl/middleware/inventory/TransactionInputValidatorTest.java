package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionInputValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TransactionInputValidatorTest {

	public static final String DEPOSIT = "Deposit";

	@InjectMocks
	private TransactionInputValidator transactionInputValidator;


	private TransactionDto transactionDto;

	@Before
	public void setup() {
		this.transactionDto = new TransactionDto();
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidatePendingStatus() {
		this.transactionDto.setAmount(10.0);
		this.transactionDto.setTransactionType(DEPOSIT);
		this.transactionDto.setNotes(RandomStringUtils.randomAlphabetic(255));
		this.transactionDto.setTransactionStatus(TransactionStatus.CONFIRMED.getValue());
		this.transactionDto.setTransactionId(1);
		List<TransactionDto> transactionDtoList = Arrays.asList(this.transactionDto);
		this.transactionInputValidator.validatePendingStatus(transactionDtoList);
	}
}
