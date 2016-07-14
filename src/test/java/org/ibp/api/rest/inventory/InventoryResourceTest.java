package org.ibp.api.rest.inventory;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.inventory.GermplasmInventory;
import org.ibp.api.domain.inventory.InventoryLocation;
import org.ibp.api.domain.ontology.TermSummary;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class InventoryResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public InventoryDataManager inventoryDataManager() {
            return Mockito.mock(InventoryDataManager.class);
        }
    }

    @Autowired
    private InventoryDataManager inventoryDataManager;

    @Test
    public void testGetInventoryLotInfoForGermplasm() throws Exception {

        Term term = new Term();
        term.setId(100);

        Location location = new Location();
        location.setLocid(9);
        location.setLname("Location name");
        location.setLabbr("lbbr");

        LotDetails lotDetails = this.createLotDetails(term, location);

        List<LotDetails> lotDetailsList = new ArrayList<>();
        lotDetailsList.add(lotDetails);

        GermplasmInventory inventory = new GermplasmInventory(1);
        inventory.setGid(1);

        Mockito.when(this.inventoryDataManager.getLotDetailsForGermplasm(Integer.valueOf("1"))).thenReturn(lotDetailsList);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/inventory/{cropname}/germplasm/{gid}", this.cropName, inventory.getGid()).contentType(this.contentType)) //
                .andDo(MockMvcResultHandlers.print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(lotDetailsList.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].lotId", Matchers.is(lotDetails.getLotId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].quantityAvailable", Matchers.is(lotDetails.getAvailableLotBalance())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].quantityReserved", Matchers.is(lotDetails.getReservedTotal())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].quantityUnit.id", Matchers.is(String.valueOf(term.getId()))))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userId", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].location.id", Matchers.is(location.getLocid())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].location.name", Matchers.is("Location name (lbbr)")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].lotStatus", Matchers.is("ACTIVE")));
    }

    @Test
    public void testCreateInventory() throws Exception {

        Lot lot = new Lot();
        lot.setId(1);

        InventoryLocation location = new InventoryLocation();
        location.setId(9);

        TermSummary termSummary = new TermSummary();
        termSummary.setId("1");

        GermplasmInventory germplasmInventory = this.createGermplasmInventory(location, termSummary);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/inventory/{cropname}/germplasm/{gid}", this.cropName, germplasmInventory.getGid()).contentType(this.contentType).content(this.convertObjectToByte(germplasmInventory))) //
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testUpdateInventory() throws Exception {

        GermplasmInventory germplasmInventory = new GermplasmInventory();

        this.mockMvc.perform(MockMvcRequestBuilders.post("/inventory/{cropname}/germplasm/{gid}" , this.cropName , 1).contentType(this.contentType).content(this.convertObjectToByte(germplasmInventory)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("This operation has not yet been implemented.")));
    }

    @Test
    public void testDeleteInventory() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/inventory/{cropname}/germplasm/{gid}" , this.cropName , 1).contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("This operation has not yet been implemented.")));
    }

	private LotDetails createLotDetails(Term scaleOfLog, Location locationOfLot) {
		LotDetails lotDetails = new LotDetails();
		lotDetails.setLotId(1);
		lotDetails.setAvailableLotBalance(10d);
		lotDetails.setReservedTotal(11d);
		lotDetails.setScaleOfLot(scaleOfLog);
		lotDetails.setLocationOfLot(locationOfLot);

		return lotDetails;
	}

	private GermplasmInventory createGermplasmInventory(InventoryLocation location, TermSummary termSummary) {
		GermplasmInventory germplasmInventory = new GermplasmInventory();
		germplasmInventory.setGid(1);
		germplasmInventory.setLotId(2);
		germplasmInventory.setQuantityAvailable(1d);
		germplasmInventory.setQuantityReserved(4d);
		germplasmInventory.setQuantityTotal(germplasmInventory.getQuantityAvailable() + germplasmInventory.getQuantityReserved());
		germplasmInventory.setUserId(99);
		germplasmInventory.setUserName("Username");
		germplasmInventory.setLocation(location);
		germplasmInventory.setQuantityUnit(termSummary);
		germplasmInventory.setLotId(1);
		germplasmInventory.setLotStatus(LotStatus.ACTIVE);
		germplasmInventory.setComments("Comments");

		return germplasmInventory;
	}
}

