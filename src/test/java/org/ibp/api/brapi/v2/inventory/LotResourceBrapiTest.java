package org.ibp.api.brapi.v2.inventory;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.hamcrest.CoreMatchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.inventory.manager.LotService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class LotResourceBrapiTest extends ApiUnitTestBase {

    @Autowired
    private LotService lotService;

    @Configuration
    public static class TestConfiguration {
        @Bean
        @Primary
        public LotService lotService() {
            return Mockito.mock(LotService.class);
        }
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
    }



    @Test
    public void testGetSeedlots() throws Exception {

        final List<ExtendedLotDto> list = new ArrayList<>();
        final ExtendedLotDto lotDto1 = new ExtendedLotDto();
        lotDto1.setActualBalance(10.0);
        lotDto1.setAvailableBalance(8.0);
        lotDto1.setCreatedByUsername("cropadmin");
        lotDto1.setStatus(LotStatus.ACTIVE.name());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        lotDto1.setCreatedDate(dateFormat.parse("2017-01-01"));
        lotDto1.setLastDepositDate(dateFormat.parse("2019-01-02"));
        lotDto1.setLastWithdrawalDate(dateFormat.parse("2019-02-03"));
        lotDto1.setDesignation(RandomStringUtils.randomAlphabetic(10));
        lotDto1.setUnitName(RandomStringUtils.randomAlphabetic(10));
        lotDto1.setStockId(RandomStringUtils.randomAlphabetic(10));
        lotDto1.setNotes(RandomStringUtils.randomAlphabetic(10));
        lotDto1.setLocationName(RandomStringUtils.randomAlphabetic(10));
        lotDto1.setLotUUID(RandomStringUtils.randomAlphabetic(10));
        final Random random = new Random();
        lotDto1.setGid(random.nextInt(10000));
        lotDto1.setLocationId(random.nextInt(10000));
        lotDto1.setLotId(random.nextInt(10000));
        lotDto1.setGermplasmUUID(String.valueOf(random.nextInt(1000)));
        list.add(lotDto1);

        Mockito.doReturn(list).when(this.lotService).searchLots(Mockito.any(LotsSearchDto.class),
                Mockito.any(Pageable.class));


        final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd");

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/{cropName}/brapi/v2/seedlots", this.cropName).contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.result.data[0].amount", CoreMatchers.is(lotDto1.getAvailableBalance())))
                .andExpect(jsonPath("$.result.data[0].createdDate", CoreMatchers.is(timestampFormat.format(lotDto1.getCreatedDate()))))
                .andExpect(jsonPath("$.result.data[0].germplasmDbId", CoreMatchers.is(lotDto1.getGermplasmUUID())))
                .andExpect(jsonPath("$.result.data[0].locationDbId", CoreMatchers.is(lotDto1.getLocationId())))
                .andExpect(jsonPath("$.result.data[0].seedLotDescription", CoreMatchers.is(lotDto1.getNotes())))
                .andExpect(jsonPath("$.result.data[0].seedLotName", CoreMatchers.is(lotDto1.getStockId())))
                .andExpect(jsonPath("$.result.data[0].storageLocation", CoreMatchers.is(lotDto1.getLocationName())))
                .andExpect(jsonPath("$.result.data[0].units", CoreMatchers.is(lotDto1.getUnitName())))
                .andExpect(jsonPath("$.result.data[0].seedLotDbId", CoreMatchers.is(lotDto1.getLotUUID())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.username", CoreMatchers.is(lotDto1.getCreatedByUsername())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.status", CoreMatchers.is(lotDto1.getStatus())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.germplasmName", CoreMatchers.is(lotDto1.getDesignation())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.actualBalance", CoreMatchers.is(lotDto1.getActualBalance())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.totalReserved", CoreMatchers.is(lotDto1.getReservedTotal())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.totalWithdrawals", CoreMatchers.is(lotDto1.getWithdrawalTotal())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.pendingDeposits", CoreMatchers.is(lotDto1.getPendingDepositsTotal())))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.lastDepositDate", CoreMatchers.is(dateFormat.format(lotDto1.getLastDepositDate()))))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.lastWithdrawalDate", CoreMatchers.is(dateFormat.format(lotDto1.getLastWithdrawalDate()))))
                .andExpect(jsonPath("$.result.data[0].additionalInfo.lotInternalId", CoreMatchers.is(lotDto1.getLotId())));

    }
}
