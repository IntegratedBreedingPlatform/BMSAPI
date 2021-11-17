package org.ibp.api.java.impl.middleware.inventory;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.ibp.api.java.impl.middleware.inventory.manager.TransactionServiceImpl;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotDepositRequestDtoValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

	@Mock
	private TransactionService transactionService;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private SecurityService securityService;

	@Mock
	private LotDepositRequestDtoValidator lotDepositRequestDtoValidator;

	@Mock
	private ExtendedLotListValidator extendedLotListValidator;

	@Mock
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Mock
	private LotService lotService;

	@InjectMocks
	private TransactionServiceImpl transactionServiceImpl;

	@Test
	public void testGetTransactions() {
		final TransactionDto transactionDto = new TransactionDto(1, "admin", TransactionType.DEPOSIT.name(), new Double(1),new Double(0),
			"comments", new Date(), 1, "1de85e1c-b947-4ee2-9d19-31eee6da9ad5", 1, "desig", "STOCK-1", 8314, "SEED_AMOUNT_g",
			LotStatus.ACTIVE.name(), TransactionStatus.CONFIRMED.getValue(), 0, "UNKNOWN", "UNKNOWN", "comments", RandomStringUtils.randomAlphanumeric(10));
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
		Assert.assertEquals(transactionDto.getLot().getGermplasmUUID(), additionalInfo.get("germplasmDbId"));
		Assert.assertEquals(String.valueOf(transactionDto.getLot().getLocationId()), additionalInfo.get("locationId"));
		Assert.assertEquals(transactionDto.getLot().getLocationName(), additionalInfo.get("locationName"));
		Assert.assertEquals(transactionDto.getLot().getLocationAbbr(), additionalInfo.get("locationAbbr"));
		Assert.assertEquals(String.valueOf(transactionDto.getLot().getUnitId()), additionalInfo.get("unitId"));
		Assert.assertEquals(transactionDto.getLot().getStockId(), additionalInfo.get("stockId"));
		Assert.assertEquals(String.valueOf(transactionDto.getLot().getLotId()), additionalInfo.get("lotId"));
		Assert.assertEquals(transactionDto.getLot().getStatus(), additionalInfo.get("lotStatus"));
		Assert.assertEquals(transactionDto.getLot().getNotes(), additionalInfo.get("lotNotes"));
		Assert.assertEquals(transactionDto.getLot().getDesignation(), additionalInfo.get("designation"));
	}

	@Test
	public void testSaveDeposits() {

		final WorkbenchUser workbenchUser = new WorkbenchUser();
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		final List<ExtendedLotDto> lotDtos = new ArrayList<>();
		final TransactionStatus transactionStatus = TransactionStatus.CONFIRMED;
		workbenchUser.setUserid(1);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(workbenchUser);

		final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
		lotDepositRequestDto.setSelectedLots(new SearchCompositeDto<>());
		this.transactionServiceImpl.saveDeposits(lotDepositRequestDto, transactionStatus);

		Mockito.verify(this.studyValidator, Mockito.times(0)).validate(ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean());
		Mockito.verify(this.lotDepositRequestDtoValidator).validate(lotDepositRequestDto);
		Mockito.verify(this.extendedLotListValidator)
			.validateAllProvidedLotUUIDsExist(lotDtos, Sets.newHashSet(lotDepositRequestDto.getSelectedLots().getItemIds()));
		Mockito.verify(this.extendedLotListValidator).validateEmptyList(lotDtos);
		Mockito.verify(this.extendedLotListValidator).validateEmptyUnits(lotDtos);
		Mockito.verify(this.extendedLotListValidator).validateClosedLots(lotDtos);
		Mockito.verify(this.lotDepositRequestDtoValidator).validateDepositInstructionsUnits(lotDepositRequestDto, lotDtos);
		Mockito.verify(this.transactionService)
			.depositLots(workbenchUser.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
				lotDepositRequestDto,
				transactionStatus,
				null, null);

	}

	@Test
	public void testSaveDepositsWithSourceStudy() {

		final WorkbenchUser workbenchUser = new WorkbenchUser();
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		final List<ExtendedLotDto> lotDtos = new ArrayList<>();
		final TransactionStatus transactionStatus = TransactionStatus.CONFIRMED;
		workbenchUser.setUserid(1);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(workbenchUser);

		final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
		lotDepositRequestDto.setSourceStudyId(99);
		lotDepositRequestDto.setSelectedLots(new SearchCompositeDto<>());
		this.transactionServiceImpl.saveDeposits(lotDepositRequestDto, transactionStatus);

		Mockito.verify(this.studyValidator).validate(lotDepositRequestDto.getSourceStudyId(), true);
		Mockito.verify(this.lotDepositRequestDtoValidator).validate(lotDepositRequestDto);
		Mockito.verify(this.extendedLotListValidator)
			.validateAllProvidedLotUUIDsExist(lotDtos, Sets.newHashSet(lotDepositRequestDto.getSelectedLots().getItemIds()));
		Mockito.verify(this.extendedLotListValidator).validateEmptyList(lotDtos);
		Mockito.verify(this.extendedLotListValidator).validateEmptyUnits(lotDtos);
		Mockito.verify(this.extendedLotListValidator).validateClosedLots(lotDtos);
		Mockito.verify(this.lotDepositRequestDtoValidator).validateDepositInstructionsUnits(lotDepositRequestDto, lotDtos);
		Mockito.verify(this.transactionService)
			.depositLots(workbenchUser.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
				lotDepositRequestDto,
				transactionStatus,
				null, null);

	}

}
