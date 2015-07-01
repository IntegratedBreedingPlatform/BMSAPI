package org.ibp.api.java.impl.middleware.germplasm;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.germplasm.GermplasmListSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GermplasmListServiceImplTest {
	
	private GermplasmListManager germplasmListManager;
	private GermplasmListService germplasmListService;

	@Before
	public void before() {
		germplasmListManager = Mockito.mock(GermplasmListManager.class);
		germplasmListService = new GermplasmListServiceImpl(germplasmListManager);	
	}
	
	@Test
	public void getAllGermplasmListTest() throws Exception {
		List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		GermplasmList germplasmList = new GermplasmList();
		setData(germplasmList);
		
		allGermplasmLists.add(germplasmList);

		when(germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		List<GermplasmListSummary> returnedValues = germplasmListService.getAllGermplasmLists();

		// First assert that the right number of summary objects are returned
		assertEquals(1, returnedValues.size());

		//assert that the list fields of the first list in the array is returned
		getAllGermplamListAssertStatements(returnedValues);
	}
	
	@Test
	public void searchGermplasmListTest() throws Exception {
		List<GermplasmList> matchingLists = new ArrayList<GermplasmList>();
		GermplasmList germplasmList = new GermplasmList();
		setData(germplasmList);
		
		matchingLists.add(germplasmList);
		
		when(germplasmListManager.searchForGermplasmList("searchText", Operation.LIKE)).thenReturn(matchingLists);
		List<GermplasmListSummary> returnedValues = germplasmListService.searchGermplasmLists("searchText");
		
		getAllGermplamListAssertStatements(returnedValues);
	}
	
	@Test(expected = ApiRuntimeException.class)
	public void getAllGermplasmListThrowsApiRuntimeExceptionTest() throws Exception {
		List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		GermplasmList germplasmList = new GermplasmList();
		setData(germplasmList);
		
		allGermplasmLists.add(germplasmList);

		when(germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenThrow(new MiddlewareQueryException("Error!"));

		germplasmListService.getAllGermplasmLists();
	}
	
	@Test(expected = ApiRuntimeException.class)
	public void searchGermplasmListThrowsApiRuntimeExceptionTest() throws Exception {
		when(germplasmListManager.searchForGermplasmList("searchText", Operation.LIKE)).thenThrow(new MiddlewareQueryException("Error!"));
		
		germplasmListService.searchGermplasmLists("searchText");
	}
	
	@Test
	public void getGermplasmListDetailsTest() throws Exception {
		GermplasmList germplasmList = new GermplasmList();		
		setData(germplasmList);
		
		when(germplasmListManager.getGermplasmListById(3)).thenReturn(germplasmList);
		GermplasmListDetails actual = germplasmListService.getGermplasmListDetails(3);
		
		assertEquals(3, actual.getListId().intValue());
		assertEquals("newName", actual.getListName());
		assertEquals("newDescription", actual.getDescription());
		assertEquals("newNotes", actual.getNotes());
				
	}
	
	@Test
	public void getGermplasmListDetailsEntryMappingTest() throws Exception {
		GermplasmList germplasmList = new GermplasmList();		
		GermplasmListData gpld = new GermplasmListData();
		gpld.setGid(10);
		gpld.setDesignation("newDesignation");
		gpld.setEntryCode("newEntryCode");
		gpld.setSeedSource("newSeedSource");
		gpld.setGroupName("newGroupName");
		List<GermplasmListData> listData = new ArrayList<GermplasmListData>();
		listData.add(gpld);
		germplasmList.setListData(listData);
				
		when(germplasmListManager.getGermplasmListById(3)).thenReturn(germplasmList);
		GermplasmListDetails actual = germplasmListService.getGermplasmListDetails(3);
		
		GermplasmListEntrySummary germplasmListEntrySummary = actual.getGermplasmEntries().get(0);
		assertEquals(10, germplasmListEntrySummary.getGid().intValue());	
		assertEquals("newDesignation", germplasmListEntrySummary.getDesignation());
		assertEquals("newEntryCode", germplasmListEntrySummary.getEntryCode());
		assertEquals("newSeedSource", germplasmListEntrySummary.getSeedSource());
		assertEquals("newGroupName", germplasmListEntrySummary.getCross());
	}
	
	@Test(expected = ApiRuntimeException.class)
	public void getGermplasmListDetailsApiRuntimeExceptionTest() throws Exception {
		when(germplasmListManager.getGermplasmListById(3)).thenThrow(new MiddlewareQueryException("Error!"));
		
		germplasmListService.getGermplasmListDetails(3);	
	}
	
	@Test
	public void getGermplasmListDetailsWithNullGermplasmListTest() throws Exception {
		GermplasmList germplasmList = new GermplasmList();		
		germplasmList = null;
		
		when(germplasmListManager.getGermplasmListById(3)).thenReturn(germplasmList);
		GermplasmListDetails actual = germplasmListService.getGermplasmListDetails(3);

		assertEquals(null, actual.getListId());
	}
	
	@Test
	public void getAllGermplasmListWithNullGermplasmListsTest() throws Exception {
		List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		allGermplasmLists = null;

		when(germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		List<GermplasmListSummary> actual = germplasmListService.getAllGermplasmLists();

		assertEquals(true, actual.isEmpty());
	}
	
	@Test
	public void getAllGermplasmListWithWithEmptyGermplasmListTest() throws Exception {
		GermplasmList germplasmList = new GermplasmList();
		List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		germplasmList.setId(0);
		germplasmList.setName("");
		germplasmList.setDescription("");
		germplasmList.setNotes("");
		germplasmList.setType("TEST_TYPE");

		when(germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		List<GermplasmListSummary> actual = germplasmListService.getAllGermplasmLists();

		assertEquals(true, actual.isEmpty());
	}
	
	@Test
	public void getAllGermplasmListWithTypeAsFolderTest() throws Exception {
		List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(3);
		germplasmList.setType("FOLDER");
		
		allGermplasmLists.add(germplasmList);

		when(germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		List<GermplasmListSummary> actual = germplasmListService.getAllGermplasmLists();

		assertEquals(true, actual.isEmpty());
	}
	
	private void getAllGermplamListAssertStatements(List<GermplasmListSummary> returnedValues) {
		GermplasmListSummary germplasmListSummary = returnedValues.get(0);
		assertEquals(3, germplasmListSummary.getListId().intValue());
		assertEquals("newName", germplasmListSummary.getListName());
		assertEquals("newDescription", germplasmListSummary.getDescription());
		assertEquals("newNotes", germplasmListSummary.getNotes());
	}

	private void setData(GermplasmList germplasmList) {
		germplasmList.setId(3);
		germplasmList.setName("newName");
		germplasmList.setDescription("newDescription");
		germplasmList.setNotes("newNotes");
		germplasmList.setType("TEST_TYPE");
	}
}
