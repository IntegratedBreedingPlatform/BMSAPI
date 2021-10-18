package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataUpdateViewDTO;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;

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
	private SecurityService securityService;

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
		Mockito.verify(this.germplasmListDataServiceMiddleware).updateGermplasmListDataView(GERMPLASM_LIST_ID, view);
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

}
