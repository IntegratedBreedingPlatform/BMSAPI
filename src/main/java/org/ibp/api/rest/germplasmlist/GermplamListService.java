package org.ibp.api.rest.germplasmlist;

import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplamListService {

	/**
	 * Search for GermplamLists with names that contain the search term
	 *
	 * @param searchString
	 * @param exactMatch
	 * @return
	 */
	List<GermplasmList> search(final String searchString, final boolean exactMatch, final String programUUID, final Pageable pageable);

}