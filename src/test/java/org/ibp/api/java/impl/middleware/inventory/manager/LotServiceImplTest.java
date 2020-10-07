package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotAdjustmentRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionSourceType;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.hamcrest.collection.IsMapContaining;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotMergeValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotServiceImplTest {

    private static final Integer USER_ID = ThreadLocalRandom.current().nextInt();
    private static final String PROGRAM_UUID = UUID.randomUUID().toString();

    @InjectMocks
    private LotServiceImpl lotService;

    @Mock
    private TransactionServiceImpl transactionService;

    @Mock
    private LotMergeValidator lotMergeValidator;

    @Mock
    private LotSplitValidator lotSplitValidator;

    @Mock
    private ExtendedLotListValidator extendedLotListValidator;

    @Mock
    private SecurityService securityService;

    @Mock
    private org.generationcp.middleware.service.api.inventory.LotService inventoryLotService;

    @Captor
    private ArgumentCaptor<LotGeneratorInputDto> lotGeneratorInputDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<LotAdjustmentRequestDto> lotAdjustmentRequestDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<LotDepositRequestDto> lotDepositRequestDtoArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldMergeLots() {
        final LotsSearchDto lotsSearchDto = Mockito.mock(LotsSearchDto.class);

        final String keepLotUUID = "keepLotUUID";
        final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
          this.createDummyExtendedLotDto(1, keepLotUUID),
          this.createDummyExtendedLotDto(2, "UUID")
        );

        Mockito.doNothing().when(this.lotMergeValidator).validate(keepLotUUID, extendedLotDtos);
        Mockito.when(this.lotService.searchLots(lotsSearchDto, null)).thenReturn(extendedLotDtos);
        Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

        this.lotService.mergeLots(keepLotUUID, lotsSearchDto);

        Mockito.verify(this.inventoryLotService).mergeLots(USER_ID, 1, lotsSearchDto);
    }

    @Test
    public void shouldSplitLot() {
        final String splitLotUUID = UUID.randomUUID().toString();
        final String unitName = "unitName";
        final double lotActualBalance = 10D;
        final LotServiceImpl lotServiceSpy = Mockito.spy(this.lotService);

        final LotSplitRequestDto lotSplitRequestDto = new LotSplitRequestDto();
        final LotSplitRequestDto.InitialLotDepositDto initialDeposit = new LotSplitRequestDto.InitialLotDepositDto();
        initialDeposit.setAmount(5D);
        initialDeposit.setNotes("Initial deposit notes");
        lotSplitRequestDto.setInitialDeposit(initialDeposit);

        final LotSplitRequestDto.NewLotSplitDto newLotSplitDto = new LotSplitRequestDto.NewLotSplitDto();
        newLotSplitDto.setLocationId(new Random().nextInt());
        newLotSplitDto.setNotes("New lot notes");
        newLotSplitDto.setGenerateStock(true);
        newLotSplitDto.setStockPrefix(UUID.randomUUID().toString());
        lotSplitRequestDto.setNewLot(newLotSplitDto);

        final ExtendedLotDto dummyExtendedLotDto = this.createDummyExtendedLotDto(1, splitLotUUID, new Random().nextInt(), unitName, new Random().nextInt(), lotActualBalance);
        final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(dummyExtendedLotDto);

        Mockito.doNothing().when(this.lotSplitValidator).validateRequest(lotSplitRequestDto);
        Mockito.doReturn(extendedLotDtos).when(lotServiceSpy).searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.isNull());
        Mockito.doNothing().when(this.extendedLotListValidator).validateAllProvidedLotUUIDsExist(ArgumentMatchers.any(List.class),
            ArgumentMatchers.any(Set.class));
        Mockito.doNothing().when(this.lotSplitValidator).validate(dummyExtendedLotDto, initialDeposit.getAmount());

        final String newLotUUID = UUID.randomUUID().toString();
        Mockito.doReturn(newLotUUID).when(lotServiceSpy).saveLot(ArgumentMatchers.eq(PROGRAM_UUID), ArgumentMatchers.any(LotGeneratorInputDto.class));

        Mockito.doNothing().when(this.transactionService).saveLotBalanceAdjustment(ArgumentMatchers.any(LotAdjustmentRequestDto.class));
        Mockito.doNothing().when(this.transactionService).saveDeposits(ArgumentMatchers.any(LotDepositRequestDto.class), ArgumentMatchers.eq(
            TransactionStatus.CONFIRMED), ArgumentMatchers.eq(TransactionSourceType.SPLIT_LOT), ArgumentMatchers.eq(dummyExtendedLotDto.getLotId()));

        lotServiceSpy.splitLot(null, lotSplitRequestDto);

        Mockito.verify(this.lotSplitValidator).validateRequest(lotSplitRequestDto);
        Mockito.verify(this.lotSplitValidator).validate(dummyExtendedLotDto, initialDeposit.getAmount());
        Mockito.verify(lotServiceSpy).saveLot(ArgumentMatchers.eq(PROGRAM_UUID), this.lotGeneratorInputDtoArgumentCaptor.capture());
        LotGeneratorInputDto actualLotGeneratorInputDto = this.lotGeneratorInputDtoArgumentCaptor.getValue();
        assertNotNull(actualLotGeneratorInputDto);
        assertThat(actualLotGeneratorInputDto.getGid(), is(dummyExtendedLotDto.getGid()));
        assertThat(actualLotGeneratorInputDto.getUnitId(), is(dummyExtendedLotDto.getUnitId()));
        assertThat(actualLotGeneratorInputDto.getLocationId(), is(newLotSplitDto.getLocationId()));
        assertThat(actualLotGeneratorInputDto.getNotes(), is(newLotSplitDto.getNotes()));
        assertThat(actualLotGeneratorInputDto.getGenerateStock(), is(newLotSplitDto.getGenerateStock()));
        assertThat(actualLotGeneratorInputDto.getStockPrefix(), is(newLotSplitDto.getStockPrefix()));

        Mockito.verify(this.transactionService).saveLotBalanceAdjustment(this.lotAdjustmentRequestDtoArgumentCaptor.capture());
        LotAdjustmentRequestDto actualLotAdjustmentRequestDto = this.lotAdjustmentRequestDtoArgumentCaptor.getValue();
        assertNotNull(actualLotAdjustmentRequestDto);
        assertThat(actualLotAdjustmentRequestDto.getBalance(), is(5D));
        assertNotNull(actualLotAdjustmentRequestDto.getSelectedLots());
        assertNotNull(actualLotAdjustmentRequestDto.getSelectedLots().getItemIds());
        assertThat(actualLotAdjustmentRequestDto.getSelectedLots().getItemIds(), hasSize(1));
        assertThat(actualLotAdjustmentRequestDto.getSelectedLots().getItemIds(), contains(dummyExtendedLotDto.getLotUUID()));

        Mockito.verify(this.transactionService).saveDeposits(this.lotDepositRequestDtoArgumentCaptor.capture(), ArgumentMatchers.eq(TransactionStatus.CONFIRMED),
            ArgumentMatchers.eq(TransactionSourceType.SPLIT_LOT), ArgumentMatchers.eq(dummyExtendedLotDto.getLotId()));
        LotDepositRequestDto actualLotDepositRequestDto = this.lotDepositRequestDtoArgumentCaptor.getValue();
        assertNotNull(actualLotDepositRequestDto);
        assertThat(actualLotDepositRequestDto.getDepositsPerUnit(), IsMapContaining.hasKey(dummyExtendedLotDto.getUnitName()));
        assertThat(actualLotDepositRequestDto.getDepositsPerUnit(), IsMapContaining.hasValue(initialDeposit.getAmount()));
        assertThat(actualLotDepositRequestDto.getNotes(), is(initialDeposit.getNotes()));
        assertNotNull(actualLotDepositRequestDto.getSelectedLots());
        assertNotNull(actualLotDepositRequestDto.getSelectedLots().getItemIds());
        assertThat(actualLotDepositRequestDto.getSelectedLots().getItemIds(), hasSize(1));
        assertThat(actualLotDepositRequestDto.getSelectedLots().getItemIds(), contains(newLotUUID));
    }

    private ExtendedLotDto createDummyExtendedLotDto(final Integer lotId, final String UUID) {
        return this.createDummyExtendedLotDto(lotId, UUID, 1, "unitName", 1, 5D);
    }

    private ExtendedLotDto createDummyExtendedLotDto(final Integer lotId, final String UUID, final Integer gid, final String unitName,
        final Integer unitId, final double actualBalance) {
        final ExtendedLotDto lotDto = new ExtendedLotDto();
        lotDto.setLotId(lotId);
        lotDto.setLotUUID(UUID);
        lotDto.setGid(gid);
        lotDto.setUnitName(unitName);
        lotDto.setUnitId(unitId);
        lotDto.setActualBalance(actualBalance);
        return lotDto;
    }

}
