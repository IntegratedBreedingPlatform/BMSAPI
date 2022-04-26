package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataUpdateViewDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListReorderEntriesRequest;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GermplasmListDataServiceImplTest {

	private static final int GERMPLASM_LIST_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final String GERMPLASM_LIST_NAME = UUID.randomUUID().toString();
	private static final Date GERMPLASM_LIST_DATE = new Date();
	private static final String GERMPLASM_LIST_DESCRIPTION = UUID.randomUUID().toString();
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();
	private static final Integer USER_ID = new Random().nextInt();

	@InjectMocks
	private GermplasmListDataServiceImpl germplasmListDataService;

	@Mock
	private GermplasmListDataService germplasmListDataServiceMiddleware;

	@Mock
	private GermplasmListValidator germplasmListValidator;

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void saveGermplasmListDataView_OK() {
		final GermplasmList germplasmList = this.createGermplasmListMock(false);
		final List<GermplasmListDataUpdateViewDTO> view = Mockito.mock(List.class);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.doNothing().when(this.germplasmListDataServiceMiddleware).updateGermplasmListDataView(GERMPLASM_LIST_ID, view);

		this.germplasmListDataService.updateGermplasmListDataView(GERMPLASM_LIST_ID, view);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListValidator).validateMaxColumnsAllowed(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListDataServiceMiddleware).updateGermplasmListDataView(GERMPLASM_LIST_ID, view);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_OK() {
		final List<Integer> selectedEntries = Arrays.asList(1, 2);
		final int entryNumberPosition = 4;
		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(selectedEntries, entryNumberPosition, null);
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.when(this.germplasmListDataServiceMiddleware.countByListId(GERMPLASM_LIST_ID)).thenReturn(5L);
		Mockito.when(this.germplasmListDataServiceMiddleware.getListDataIdsByListId(GERMPLASM_LIST_ID)).thenReturn(selectedEntries);
		Mockito.doNothing().when(this.germplasmListDataServiceMiddleware)
			.reOrderEntries(GERMPLASM_LIST_ID, selectedEntries, entryNumberPosition);

		this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListDataServiceMiddleware).countByListId(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListDataServiceMiddleware).getListDataIdsByListId(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListDataServiceMiddleware).reOrderEntries(GERMPLASM_LIST_ID, selectedEntries, entryNumberPosition);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_endOfList_OK() {
		final List<Integer> selectedEntries = Arrays.asList(1, 2);
		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(selectedEntries, null, true);
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.when(this.germplasmListDataServiceMiddleware.countByListId(GERMPLASM_LIST_ID)).thenReturn(5L);
		Mockito.when(this.germplasmListDataServiceMiddleware.getListDataIdsByListId(GERMPLASM_LIST_ID)).thenReturn(selectedEntries);
		Mockito.doNothing().when(this.germplasmListDataServiceMiddleware).reOrderEntries(GERMPLASM_LIST_ID, selectedEntries, 4);

		this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListDataServiceMiddleware).countByListId(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListDataServiceMiddleware).getListDataIdsByListId(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListDataServiceMiddleware).reOrderEntries(GERMPLASM_LIST_ID, selectedEntries, 4);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_numberOfEntriesEqualsToSelectedEntries_OK() {
		final List<Integer> selectedEntries = Arrays.asList(1, 2);
		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(selectedEntries, null, true);
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.when(this.germplasmListDataServiceMiddleware.countByListId(GERMPLASM_LIST_ID)).thenReturn(new Long(selectedEntries.size()));

		this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListDataServiceMiddleware).countByListId(GERMPLASM_LIST_ID);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_invalidNullRequest() {

		try {
			this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, null);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("list.reorder.input.null"));
		}

		Mockito.verifyNoInteractions(this.germplasmListValidator);
		Mockito.verifyNoInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_invalidEmptySelectedEntries() {

		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(null, 1, null);

		try {
			this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("list.reorder.selected.entries.empty"));
		}

		Mockito.verifyNoInteractions(this.germplasmListValidator);
		Mockito.verifyNoInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_invalidNullSelectedPositionNumber() {
		final List<Integer> selectedEntries = Arrays.asList(1, 2, 3);
		final int entryNumberPosition = 4;
		final long totalEntries = 5L;
		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(selectedEntries, null, null);
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.when(this.germplasmListDataServiceMiddleware.countByListId(GERMPLASM_LIST_ID)).thenReturn(totalEntries);
		Mockito.doNothing().when(this.germplasmListDataServiceMiddleware)
			.reOrderEntries(GERMPLASM_LIST_ID, selectedEntries, entryNumberPosition);

		try {
			this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("list.reorder.invalid.selected.position.number"));
			assertThat(e.getErrors().get(0).getArguments().length, is(1));
			assertThat(e.getErrors().get(0).getArguments()[0], is(String.valueOf(totalEntries - selectedEntries.size() + 1)));
		}

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListDataServiceMiddleware).countByListId(GERMPLASM_LIST_ID);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_invalidNegativeSelectedPositionNumber() {
		final List<Integer> selectedEntries = Arrays.asList(1, 2, 3);
		final int entryNumberPosition = 4;
		final long totalEntries = 5L;
		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(selectedEntries, -1, null);
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.when(this.germplasmListDataServiceMiddleware.countByListId(GERMPLASM_LIST_ID)).thenReturn(totalEntries);
		Mockito.doNothing().when(this.germplasmListDataServiceMiddleware)
			.reOrderEntries(GERMPLASM_LIST_ID, selectedEntries, entryNumberPosition);

		try {
			this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("list.reorder.invalid.selected.position.number"));
			assertThat(e.getErrors().get(0).getArguments().length, is(1));
			assertThat(e.getErrors().get(0).getArguments()[0], is(String.valueOf(totalEntries - selectedEntries.size() + 1)));
		}

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListDataServiceMiddleware).countByListId(GERMPLASM_LIST_ID);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_invalidSelectedPositionOutOfRange() {
		final List<Integer> selectedEntries = Arrays.asList(1, 2, 3);
		final int entryNumberPosition = 4;
		final long totalEntries = 5L;
		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(selectedEntries, entryNumberPosition, null);
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.when(this.germplasmListDataServiceMiddleware.countByListId(GERMPLASM_LIST_ID)).thenReturn(totalEntries);
		Mockito.doNothing().when(this.germplasmListDataServiceMiddleware)
			.reOrderEntries(GERMPLASM_LIST_ID, selectedEntries, entryNumberPosition);

		try {
			this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("list.reorder.invalid.selected.position"));
			assertThat(e.getErrors().get(0).getArguments().length, is(3));
			assertThat(e.getErrors().get(0).getArguments()[0], is(String.valueOf(entryNumberPosition)));
			assertThat(e.getErrors().get(0).getArguments()[1], is(String.valueOf(totalEntries)));
			assertThat(e.getErrors().get(0).getArguments()[2], is(String.valueOf(totalEntries - selectedEntries.size() + 1)));
		}

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListDataServiceMiddleware).countByListId(GERMPLASM_LIST_ID);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void reOrderEntries_selectedEntriesNotInList() {
		final List<Integer> entriesInList = Arrays.asList(4, 5, 6);
		final List<Integer> selectedEntries = Arrays.asList(1, 2, 3);
		final int entryNumberPosition = 1;
		final long totalEntries = 5L;
		final GermplasmListReorderEntriesRequest request =
			this.createDummyGermplasmListReorderEntriesRequest(selectedEntries, entryNumberPosition, null);
		final GermplasmList germplasmList = this.createGermplasmListMock(false);

		Mockito.when(this.germplasmListValidator.validateGermplasmList(GERMPLASM_LIST_ID)).thenReturn(germplasmList);
		Mockito.doNothing().when(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.when(this.germplasmListDataServiceMiddleware.countByListId(GERMPLASM_LIST_ID)).thenReturn(totalEntries);
		Mockito.when(this.germplasmListDataServiceMiddleware.getListDataIdsByListId(GERMPLASM_LIST_ID)).thenReturn(entriesInList);

		try {
			this.germplasmListDataService.reOrderEntries(GERMPLASM_LIST_ID, request);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("list.entries.not.in.list"));
			assertThat(e.getErrors().get(0).getArguments().length, is(1));
			assertThat(e.getErrors().get(0).getArguments()[0], is("1,2,3"));
		}

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListValidator).validateListIsUnlocked(germplasmList);
		Mockito.verify(this.germplasmListDataServiceMiddleware).countByListId(GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListDataServiceMiddleware).getListDataIdsByListId(GERMPLASM_LIST_ID);

		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListDataServiceMiddleware);
	}

	@Test
	public void testGetGermplasmListDataDetailList_OK() {
		final int listId = 1;
		this.germplasmListDataService.getGermplasmListDataDetailList(1);
		Mockito.verify(this.germplasmListDataServiceMiddleware).getGermplasmListDataDetailList(listId);
	}

	private GermplasmList createGermplasmListMock(final boolean isLocked) {
		final GermplasmList mock = Mockito.mock(GermplasmList.class);
		Mockito.when(mock.getId()).thenReturn(GERMPLASM_LIST_ID);
		Mockito.when(mock.getName()).thenReturn(GERMPLASM_LIST_NAME);
		Mockito.when(mock.parseDate()).thenReturn(GERMPLASM_LIST_DATE);
		Mockito.when(mock.getDescription()).thenReturn(GERMPLASM_LIST_DESCRIPTION);
		Mockito.when(mock.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(mock.getUserId()).thenReturn(USER_ID);
		Mockito.when(mock.isLockedList()).thenReturn(isLocked);
		return mock;
	}

	private GermplasmListReorderEntriesRequest createDummyGermplasmListReorderEntriesRequest(final List<Integer> selectedEntries,
		final Integer entryNumberPosition, final Boolean endOfList) {
		final GermplasmListReorderEntriesRequest request = new GermplasmListReorderEntriesRequest();
		request.setSelectedEntries(selectedEntries);
		request.setEntryNumberPosition(entryNumberPosition);
		request.setEndOfList(endOfList);
		return request;
	}

}
