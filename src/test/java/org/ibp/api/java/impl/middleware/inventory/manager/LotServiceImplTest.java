package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotMergeValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(MockitoJUnitRunner.class)
public class LotServiceImplTest {

    private static final Integer USER_ID = ThreadLocalRandom.current().nextInt();

    @InjectMocks
    private LotServiceImpl lotService;

    @Mock
    private LotMergeValidator lotMergeValidator;

    @Mock
    private SecurityService securityService;

    @Mock
    private org.generationcp.middleware.service.api.inventory.LotService inventoryLotService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldMergeLots() {
        final LotsSearchDto lotsSearchDto = Mockito.mock(LotsSearchDto.class);

        String keepLotUUID = "keepLotUUID";
        List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
          this.createDummyExtendedLotDto(1, keepLotUUID),
          this.createDummyExtendedLotDto(2, "UUID")
        );

        Mockito.doNothing().when(this.lotMergeValidator).validate(keepLotUUID, extendedLotDtos);
        Mockito.when(this.lotService.searchLots(lotsSearchDto, null)).thenReturn(extendedLotDtos);
        Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

        this.lotService.mergeLots(keepLotUUID, lotsSearchDto);

        Mockito.verify(this.inventoryLotService).mergeLots(USER_ID, 1, lotsSearchDto);
    }

    private ExtendedLotDto createDummyExtendedLotDto(Integer lotId, String UUID) {
        final ExtendedLotDto lotDto = new ExtendedLotDto();
        lotDto.setLotId(lotId);
        lotDto.setLotUUID(UUID);
        return lotDto;
    }

}
