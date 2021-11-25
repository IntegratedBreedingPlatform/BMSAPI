package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListDataValidatorTest {

	@Mock
	private GermplasmListDataService germplasmListDataService;

	@InjectMocks
	private GermplasmListDataValidator germplasmListDataValidator;

	@Test
	public void testVerifyListDataIdsExist_Fail() {
		final int listId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		when(this.germplasmListDataService.searchGermplasmListData(eq(listId), any(GermplasmListDataSearchRequest.class),
			eq(null))).thenReturn(
			Arrays.asList(new GermplasmListDataSearchResponse()));
		final List<Integer> selectedEntries = Arrays.asList(1, 2);
		try {
			this.germplasmListDataValidator.verifyListDataIdsExist(listId, selectedEntries);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(e.getErrors().get(0).getCode(), is("germplasm.list.data.ids.not.exist"));
		}
	}

	@Test
	public void testVerifyListDataIdsExist_Ok() {
		final int listId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		when(this.germplasmListDataService.searchGermplasmListData(eq(listId), any(GermplasmListDataSearchRequest.class),
			eq(null))).thenReturn(
			Arrays.asList(new GermplasmListDataSearchResponse()));
		try {
			this.germplasmListDataValidator.verifyListDataIdsExist(listId, Arrays.asList(1));
		} catch (final Exception e) {
			Assert.fail("Should not have thrown validation exception");
		}

	}
}
