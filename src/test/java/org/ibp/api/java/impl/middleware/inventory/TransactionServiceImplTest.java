package org.ibp.api.java.impl.middleware.inventory;

import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.ibp.api.java.impl.middleware.inventory.manager.TransactionServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

	@Mock
	private TransactionService transactionService;

	@InjectMocks
	private TransactionServiceImpl transactionServiceImpl;

	@Test
	public void testGetTransactions() {
		final TransactionDto transactionDto = new TransactionDto(1, "admin", TransactionType.DEPOSIT.name(), new Double(1),
			"comments", new Date(), 1, "1de85e1c-b947-4ee2-9d19-31eee6da9ad5", 1, "desig", "STOCK-1", 8314, "SEED_AMOUNT_g",
			LotStatus.ACTIVE.name(), TransactionStatus.CONFIRMED.getValue(), 0, "UNKNOWN", "UNKNOWN", "comments");
		Mockito.when(this.transactionService.searchTransactions(ArgumentMatchers.any(TransactionsSearchDto.class), ArgumentMatchers.any(
			Pageable.class))).thenReturn(Collections.singletonList(transactionDto));
		final Pageable pageable = Mockito.mock(Pageable.class);
		final List<org.ibp.api.brapi.v2.inventory.TransactionDto>
			transactionDtoList = this.transactionServiceImpl.getTransactions(new TransactionsSearchDto(), pageable);

		Assert.assertEquals(1, transactionDtoList.size());
		final org.ibp.api.brapi.v2.inventory.TransactionDto transformedTransaction = transactionDtoList.get(0);
		Assert.assertEquals(transactionDto.getTransactionId().toString(), transformedTransaction.getTransactionDbId());
		Assert.assertEquals(transactionDto.getLot().getUnitName(), transformedTransaction.getUnits());
		Assert.assertEquals(transactionDto.getCreatedDate(), transformedTransaction.getTransactionTimestamp());
		Assert.assertEquals(transactionDto.getNotes(), transformedTransaction.getTransactionDescription());
		Assert.assertEquals(transactionDto.getAmount(), transformedTransaction.getAmount());
		final Map<String, Object> additionalInfo = transformedTransaction.getAdditionalInfo();
		Assert.assertEquals(transactionDto.getCreatedByUsername(), additionalInfo.get("createdByUsername"));
		Assert.assertEquals(transactionDto.getTransactionType(), additionalInfo.get("transactionType"));
		Assert.assertEquals(transactionDto.getTransactionStatus(), additionalInfo.get("transactionStatus"));
		Assert.assertEquals(transactionDto.getLot().getLotUUID(), additionalInfo.get("seedLotID"));
		Assert.assertEquals(transactionDto.getLot().getGid(), additionalInfo.get("germplasmDbId"));
		Assert.assertEquals(transactionDto.getLot().getLocationId(), additionalInfo.get("locationId"));
		Assert.assertEquals(transactionDto.getLot().getLocationName(), additionalInfo.get("locationName"));
		Assert.assertEquals(transactionDto.getLot().getLocationAbbr(), additionalInfo.get("locationAbbr"));
		Assert.assertEquals(transactionDto.getLot().getUnitId(), additionalInfo.get("unitId"));
		Assert.assertEquals(transactionDto.getLot().getStockId(), additionalInfo.get("stockId"));
		Assert.assertEquals(transactionDto.getLot().getLotId(), additionalInfo.get("lotId"));
		Assert.assertEquals(transactionDto.getLot().getStatus(), additionalInfo.get("lotStatus"));
		Assert.assertEquals(transactionDto.getLot().getNotes(), additionalInfo.get("lotNotes"));
		Assert.assertEquals(transactionDto.getLot().getDesignation(), additionalInfo.get("designation"));
	}

}
