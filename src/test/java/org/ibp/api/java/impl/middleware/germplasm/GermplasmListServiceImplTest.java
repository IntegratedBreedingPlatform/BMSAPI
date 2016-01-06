
package org.ibp.api.java.impl.middleware.germplasm;

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
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class GermplasmListServiceImplTest {

	private GermplasmListManager germplasmListManager;
	private GermplasmListService germplasmListService;
	private SecurityService securityService;
	private static final String PROGRAM_UUID = "a7433c01-4f46-4bc8-ae3a-678f0b62ac23";

	@Before
	public void before() {
		this.germplasmListManager = Mockito.mock(GermplasmListManager.class);
		this.securityService = Mockito.mock(SecurityService.class);
		// Make all lists accessible
		Mockito.when(this.securityService.isAccessible(Matchers.any(GermplasmList.class))).thenReturn(true);
		this.germplasmListService = new GermplasmListServiceImpl(this.germplasmListManager, this.securityService);
	}

	@Test
	public void getAllGermplasmListTest() throws Exception {
		final List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		final GermplasmList germplasmList = new GermplasmList();
		this.setData(germplasmList);

		allGermplasmLists.add(germplasmList);

		Mockito.when(this.germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		final List<GermplasmListSummary> returnedValues = this.germplasmListService.list();

		// First assert that the right number of summary objects are returned
		Assert.assertEquals(1, returnedValues.size());

		// assert that the list fields of the first list in the array is returned
		this.getAllGermplamListAssertStatements(returnedValues);
	}

	@Test
	public void searchGermplasmListTest() throws Exception {
		final List<GermplasmList> matchingLists = new ArrayList<GermplasmList>();
		final GermplasmList germplasmList = new GermplasmList();
		this.setData(germplasmList);

		matchingLists.add(germplasmList);

		Mockito.when(
				this.germplasmListManager.searchForGermplasmList("searchText", GermplasmListServiceImplTest.PROGRAM_UUID, Operation.LIKE))
				.thenReturn(matchingLists);
		final List<GermplasmListSummary> returnedValues =
				this.germplasmListService.searchGermplasmLists("searchText", GermplasmListServiceImplTest.PROGRAM_UUID);

		this.getAllGermplamListAssertStatements(returnedValues);
	}

	@Test(expected = ApiRuntimeException.class)
	public void getAllGermplasmListThrowsApiRuntimeExceptionTest() throws Exception {
		final List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		final GermplasmList germplasmList = new GermplasmList();
		this.setData(germplasmList);

		allGermplasmLists.add(germplasmList);

		Mockito.when(this.germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE))
				.thenThrow(new MiddlewareQueryException("Error!"));

		this.germplasmListService.list();
	}

	@Test(expected = ApiRuntimeException.class)
	public void searchGermplasmListThrowsApiRuntimeExceptionTest() throws Exception {
		Mockito.when(
				this.germplasmListManager.searchForGermplasmList("searchText", GermplasmListServiceImplTest.PROGRAM_UUID, Operation.LIKE))
				.thenThrow(new MiddlewareQueryException("Error!"));

		this.germplasmListService.searchGermplasmLists("searchText", GermplasmListServiceImplTest.PROGRAM_UUID);
	}

	@Test
	public void getGermplasmListDetailsTest() throws Exception {
		final GermplasmList germplasmList = new GermplasmList();
		this.setData(germplasmList);

		Mockito.when(this.germplasmListManager.getGermplasmListById(3)).thenReturn(germplasmList);
		final GermplasmListDetails actual = this.germplasmListService.getGermplasmListDetails(3);

		Assert.assertEquals(3, actual.getListId().intValue());
		Assert.assertEquals("newName", actual.getListName());
		Assert.assertEquals("newDescription", actual.getDescription());
		Assert.assertEquals("newNotes", actual.getNotes());

	}

	@Test
	public void getGermplasmListDetailsEntryMappingTest() throws Exception {
		final GermplasmList germplasmList = new GermplasmList();
		final GermplasmListData gpld = new GermplasmListData();
		gpld.setGid(10);
		gpld.setDesignation("newDesignation");
		gpld.setEntryCode("newEntryCode");
		gpld.setSeedSource("newSeedSource");
		gpld.setGroupName("newGroupName");
		final List<GermplasmListData> listData = new ArrayList<GermplasmListData>();
		listData.add(gpld);
		germplasmList.setListData(listData);

		Mockito.when(this.germplasmListManager.getGermplasmListById(3)).thenReturn(germplasmList);
		final GermplasmListDetails actual = this.germplasmListService.getGermplasmListDetails(3);

		final GermplasmListEntrySummary germplasmListEntrySummary = actual.getGermplasmEntries().get(0);
		Assert.assertEquals(10, germplasmListEntrySummary.getGid().intValue());
		Assert.assertEquals("newDesignation", germplasmListEntrySummary.getDesignation());
		Assert.assertEquals("newEntryCode", germplasmListEntrySummary.getEntryCode());
		Assert.assertEquals("newSeedSource", germplasmListEntrySummary.getSeedSource());
		Assert.assertEquals("newGroupName", germplasmListEntrySummary.getCross());
	}

	@Test(expected = ApiRuntimeException.class)
	public void getGermplasmListDetailsApiRuntimeExceptionTest() throws Exception {
		Mockito.when(this.germplasmListManager.getGermplasmListById(3)).thenThrow(new MiddlewareQueryException("Error!"));

		this.germplasmListService.getGermplasmListDetails(3);
	}

	@Test
	public void getGermplasmListDetailsWithNullGermplasmListTest() throws Exception {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList = null;

		Mockito.when(this.germplasmListManager.getGermplasmListById(3)).thenReturn(germplasmList);
		final GermplasmListDetails actual = this.germplasmListService.getGermplasmListDetails(3);

		Assert.assertEquals(null, actual.getListId());
	}

	@Test
	public void getAllGermplasmListWithNullGermplasmListsTest() throws Exception {
		List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		allGermplasmLists = null;

		Mockito.when(this.germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		final List<GermplasmListSummary> actual = this.germplasmListService.list();

		Assert.assertEquals(true, actual.isEmpty());
	}

	@Test
	public void getAllGermplasmListWithWithEmptyGermplasmListTest() throws Exception {
		final GermplasmList germplasmList = new GermplasmList();
		final List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		germplasmList.setId(0);
		germplasmList.setName("");
		germplasmList.setDescription("");
		germplasmList.setNotes("");
		germplasmList.setType("TEST_TYPE");

		Mockito.when(this.germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		final List<GermplasmListSummary> actual = this.germplasmListService.list();

		Assert.assertEquals(true, actual.isEmpty());
	}

	@Test
	public void getAllGermplasmListWithTypeAsFolderTest() throws Exception {
		final List<GermplasmList> allGermplasmLists = new ArrayList<GermplasmList>();
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(3);
		germplasmList.setType("FOLDER");

		allGermplasmLists.add(germplasmList);

		Mockito.when(this.germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE)).thenReturn(allGermplasmLists);
		final List<GermplasmListSummary> actual = this.germplasmListService.list();

		Assert.assertEquals(true, actual.isEmpty());
	}

	private void getAllGermplamListAssertStatements(final List<GermplasmListSummary> returnedValues) {
		final GermplasmListSummary germplasmListSummary = returnedValues.get(0);
		Assert.assertEquals(3, germplasmListSummary.getListId().intValue());
		Assert.assertEquals("newName", germplasmListSummary.getListName());
		Assert.assertEquals("newDescription", germplasmListSummary.getDescription());
		Assert.assertEquals("newNotes", germplasmListSummary.getNotes());
	}

	private void setData(final GermplasmList germplasmList) {
		germplasmList.setId(3);
		germplasmList.setName("newName");
		germplasmList.setDescription("newDescription");
		germplasmList.setNotes("newNotes");
		germplasmList.setType("TEST_TYPE");
	}
}
