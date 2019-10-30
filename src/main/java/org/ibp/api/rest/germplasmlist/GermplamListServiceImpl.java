package org.ibp.api.rest.germplasmlist;

import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.NEVER)
public class GermplamListServiceImpl implements GermplamListService {

	@Autowired
	private GermplasmListManager germplamListService;

	@Override
	public List<GermplasmList> search(
		final String searchString, final boolean exactMatch, final String programUUID, final Pageable pageable) {
		return this.germplamListService.searchGermplasmLists(searchString, exactMatch, programUUID, pageable);
	}
}