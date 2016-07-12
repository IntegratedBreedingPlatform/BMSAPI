package org.ibp.api.rest.germplasm;

import java.util.List;

import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProperty;
import org.generationcp.middleware.pojos.Name;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.data.initializer.GermplasmTestDataProvider;
import org.ibp.api.domain.germplasm.GermplasmListSummary;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;

public class GermplasmResourceListTest extends ApiUnitTestBase{


    @Mock
    private GermplasmDataManager germplasmDataManager;

    @Autowired
    private GermplasmListManager listManager;

    @Mock
    private LocationDataManager locationDataManger;

    private Germplasm germplasm = new Germplasm();

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test to search germplasmlist based on search string
     *
     * @throws Exception
     */

    @Test
    public void testSearchGermplasmList() throws Exception{
        Germplasm gp = GermplasmTestDataProvider.createGermplasm();

        List<Germplasm> middlewareSearchResults = Lists.newArrayList(gp);

        Name gpName = new Name();
        gpName.setGermplasmId(gp.getGid());
        gpName.setNval("New");
        List<Name> gpNames = Lists.newArrayList(gpName);
        Mockito.when(this.germplasmDataManager.getNamesByGID(gp.getGid(), 0 , GermplasmNameType.ABBREVIATED_CULTIVAR_NAME))
                .thenReturn(gpNames);

        String searchString = "New";

        Mockito.when(this.germplasmDataManager.searchForGermplasm(searchString, Operation.LIKE, false, false, false)).thenReturn(middlewareSearchResults);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/germplasmList//{cropname}/search?q=" +searchString , this.cropName)
            .contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGermplasamList() throws Exception{
        Germplasm germplasm = new Germplasm();
        germplasm.setGrplce(1);

        Mockito.when(this.germplasmDataManager.getAllGermplasm(0, Integer.MAX_VALUE)).thenReturn(Lists.newArrayList(germplasm));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/germplasmList/{cropname}/list" , this.cropName)
                .contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetGermplasmListDetails() throws Exception{

        germplasm.setGid(3);
        germplasm.setGpid1(1);
        germplasm.setGpid2(2);
        germplasm.setMethodId(1);
        germplasm.setLocationId(1);

        Name n = new Name();
        n.setTypeId(1);
        n.setNval("Test Germplasm");
        this.germplasm.setPreferredName(n);

        GermplasmListSummary summary = new GermplasmListSummary();
        summary.setListId(1);
        summary.setListName("Germplasm");
        summary.setListSize(1);
        summary.setDescription("Germplasm List");
        summary.setNotes("Notes");

        GermplasmList list = new GermplasmList();
        list.setId(1);
        list.setName(summary.getListName());
        list.setNotes(summary.getNotes());
        list.setDescription(summary.getDescription());

        GermplasmListData listData = new GermplasmListData();
        listData.setId(1);
        listData.setList(list);
        listData.setGid(3);

        ListDataInventory inventory = new ListDataInventory(list.getId() , germplasm.getGid());
        inventory.setGid(germplasm.getGid());
        inventory.setListDataId(listData.getId());

        listData.setInventoryInfo(inventory);
        listData.setGermplasm(germplasm);

        ListDataProperty property = new ListDataProperty();
        property.setColumn("list");
        property.setListData(listData);

        listData.setProperties(Lists.newArrayList(property));
        listData.setDesignation("Designation");
        listData.setSeedSource("Seed Source");
        listData.setEntryCode("E1");
        listData.setGroupId(1);
        listData.setGroupName("Germplasm List");

        list.setListData(Lists.newArrayList(listData));
        list.setParent(list);
        list.setProgramUUID(this.programUuid);

        Mockito.when(this.germplasmDataManager.getGermplasmByGID(this.germplasm.getGid())).thenReturn(this.germplasm);
        Mockito.when(this.listManager.getGermplasmListById(list.getId())).thenReturn(list);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/germplasmList/{cropname}/{list}" , this.cropName , summary.getListId())
            .contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.listId", org.hamcrest.Matchers.is(list.getId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.listName", org.hamcrest.Matchers.is(list.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", org.hamcrest.Matchers.is(list.getDescription())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.notes", org.hamcrest.Matchers.is(list.getNotes())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.listSize", org.hamcrest.Matchers.is(list.getListData().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmEntries[0].gid", org.hamcrest.Matchers.is(listData.getGid())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmEntries[0].designation", org.hamcrest.Matchers.is(listData.getDesignation())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmEntries[0].seedSource", org.hamcrest.Matchers.is(listData.getSeedSource())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmEntries[0].entryCode", org.hamcrest.Matchers.is(listData.getEntryCode())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmEntries[0].cross", org.hamcrest.Matchers.is(listData.getGroupName())));
    }

}
