package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Component
public class GermplasmDeleteValidator {

	@Resource
	private GermplasmService germplasmService;

	public Set<Integer> checkInvalidGidsForDeletion(final List<Integer> gids) {

		final Set<Integer> codeFixedGermplasms = this.germplasmService.getCodeFixedGidsByGidList(gids);
		final Set<Integer> germplasmWithDescendants = this.germplasmService.getGidsOfGermplasmWithDescendants(gids);
		final Set<Integer> germplasmUsedInStudies = this.germplasmService.getGermplasmUsedInStudies(gids);
		final Set<Integer> germplasmWithOpenLots = this.germplasmService.getGidsWithOpenLots(gids);
		final Set<Integer> germplasmInOneOrMoreLists = this.germplasmService.getGermplasmUsedInOneOrMoreList(gids);

		return Sets.newHashSet(Iterables
			.concat(codeFixedGermplasms, germplasmWithDescendants, germplasmUsedInStudies, germplasmWithOpenLots,
				germplasmInOneOrMoreLists));

	}

}
