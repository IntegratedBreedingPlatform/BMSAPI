package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class GermplasmDeleteValidator {

	@Resource
	private GermplasmService germplasmMiddlewareService;

	public Set<Integer> checkInvalidGidsForDeletion(final List<Integer> gids) {

		final Set<Integer> codeFixedGermplasms = this.germplasmMiddlewareService.getCodeFixedGidsByGidList(gids);
		final Set<Integer> germplasmWithDescendants = this.germplasmMiddlewareService.getGidsOfGermplasmWithDescendants(gids, Collections.emptyList());
		final Set<Integer> germplasmUsedInStudies = this.germplasmMiddlewareService.getGermplasmUsedInStudies(gids);
		final Set<Integer> germplasmWithOpenLots = this.germplasmMiddlewareService.getGidsWithOpenLots(gids);
		final Set<Integer> germplasmInOneOrMoreLists = this.germplasmMiddlewareService.getGermplasmUsedInOneOrMoreList(gids);

		return Sets.newHashSet(Iterables
			.concat(codeFixedGermplasms, germplasmWithDescendants, germplasmUsedInStudies, germplasmWithOpenLots,
				germplasmInOneOrMoreLists));

	}

}
