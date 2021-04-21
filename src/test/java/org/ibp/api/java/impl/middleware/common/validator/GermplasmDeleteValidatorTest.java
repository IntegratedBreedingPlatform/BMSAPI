package org.ibp.api.java.impl.middleware.common.validator;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmDeleteValidatorTest {

	@Mock
	private GermplasmService germplasmService;

	@InjectMocks
	private GermplasmDeleteValidator germplasmDeleteValidator;

	@Test
	public void testCheckInvalidGidsForDeletion() {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3, 4, 5);

		when(this.germplasmService.getCodeFixedGidsByGidList(gids)).thenReturn(Sets.newHashSet(1));
		when(this.germplasmService.getGidsOfGermplasmWithDescendants(gids)).thenReturn(Sets.newHashSet(2));
		when(this.germplasmService.getGermplasmUsedInStudies(gids)).thenReturn(Sets.newHashSet(3));
		when(this.germplasmService.getGidsWithOpenLots(gids)).thenReturn(Sets.newHashSet(4));
		when(this.germplasmService.getGermplasmUsedInLockedList(gids)).thenReturn(Sets.newHashSet(5));

		final Set<Integer> result = this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids);

		Assert.assertEquals(gids, new ArrayList<>(result));

	}

}
