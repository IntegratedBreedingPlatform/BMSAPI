package org.ibp.api.java.impl.middleware.inventory;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.impl.middleware.common.validator.InventoryScaleValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionInputValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TransactionInputValidatorTest {

	@InjectMocks
	private TransactionInputValidator transactionInputValidator;

	@Mock
	private InventoryScaleValidator inventoryScaleValidator;

	@Mock
	private LotService lotService;
	private TransactionDto transactionDto;

	@Before
	public void setup() {
		this.transactionDto = new TransactionDto();
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataAmount() {
		this.transactionDto.setAmount(0.0);
		this.transactionDto.setTransactionType("Deposit");
		this.transactionDto.setNotes("notes");

		this.transactionInputValidator.validate(this.transactionDto);
	}

	@Test(expected = NotSupportedException.class)
	public void testValidateDataTransactionType() {
		this.transactionDto.setAmount(10.0);
		this.transactionDto.setTransactionType("Reservation");
		this.transactionDto.setNotes("notes");

		this.transactionInputValidator.validate(this.transactionDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataNotes() {
		this.transactionDto.setAmount(10.0);
		this.transactionDto.setTransactionType("Deposit");
		this.transactionDto.setNotes(
			"notesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotesnotes");

		this.transactionInputValidator.validate(this.transactionDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validateCloseLot() {
		this.transactionDto.setAmount(10.0);
		this.transactionDto.setTransactionType("Deposit");
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		lotsSearchDto.setLotIds(Lists.newArrayList(this.transactionDto.getLot().getLotId()));
		final ExtendedLotDto lot = new ExtendedLotDto();
		lot.setScaleId(1);
		lot.setStatus(LotStatus.CLOSED.name());
		final List<ExtendedLotDto> result = Lists.newArrayList(lot);
		Mockito.when(this.lotService.searchLots(lotsSearchDto, null)).thenReturn(result);
		this.transactionInputValidator.validate(this.transactionDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validateEmptyLot() {
		this.transactionDto.setAmount(10.0);
		this.transactionDto.setTransactionType("Deposit");
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		lotsSearchDto.setLotIds(Lists.newArrayList(this.transactionDto.getLot().getLotId()));

		Mockito.when(this.lotService.searchLots(lotsSearchDto, null)).thenReturn(Lists.newArrayList());
		this.transactionInputValidator.validate(this.transactionDto);
	}
}
