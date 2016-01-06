
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GermplasmListServiceImpl implements GermplasmListService {

	private static final String ERROR_NAME = "Error!";

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SecurityService securityService;

	public GermplasmListServiceImpl() {

	}

	/**
	 * Only used for testing
	 *
	 * @param germplasmListManager the mock germplasm list manager
	 */
	GermplasmListServiceImpl(final GermplasmListManager germplasmListManager, final SecurityService securityService) {
		this.germplasmListManager = germplasmListManager;
		this.securityService = securityService;
	}

	@Override
	public List<GermplasmListSummary> searchGermplasmLists(String searchText, String programUUID) {
		List<GermplasmList> matchingLists;
		try {
			matchingLists = this.germplasmListManager.searchForGermplasmList(searchText, programUUID, Operation.LIKE);
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException(GermplasmListServiceImpl.ERROR_NAME, e);
		}
		return this.mapResults(matchingLists);
	}

	private List<GermplasmListSummary> mapResults(List<GermplasmList> germplasmLists) {
		List<GermplasmListSummary> results = new ArrayList<GermplasmListSummary>();
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			for (GermplasmList gpList : germplasmLists) {
				if (!this.securityService.isAccessible(gpList)) {
					continue;
				}
				// FIXME hack to remove folders. Middleware service should offer this option and handle it internally!
				if (!gpList.getType().equals("FOLDER")) {
					GermplasmListSummary res = new GermplasmListSummary();
					res.setListId(gpList.getId());
					res.setListName(gpList.getName());
					res.setDescription(gpList.getDescription());
					res.setNotes(gpList.getNotes());
					res.setListSize(gpList.getListData().size());
					results.add(res);
				}
			}
		}
		return results;
	}

	@Override
	public GermplasmListDetails getGermplasmListDetails(Integer listId) {

		GermplasmListDetails listDetails = new GermplasmListDetails();
		GermplasmList gpList;
		try {
			gpList = this.germplasmListManager.getGermplasmListById(listId);

			if (gpList != null) {
				listDetails.setListId(gpList.getId());
				listDetails.setListName(gpList.getName());
				listDetails.setDescription(gpList.getDescription());
				listDetails.setNotes(gpList.getNotes());
				listDetails.setListSize(gpList.getListData().size());

				for (GermplasmListData gpListData : gpList.getListData()) {
					GermplasmListEntrySummary gpEntry = new GermplasmListEntrySummary();
					gpEntry.setGid(gpListData.getGid());
					gpEntry.setDesignation(gpListData.getDesignation());
					gpEntry.setEntryCode(gpListData.getEntryCode());
					gpEntry.setSeedSource(gpListData.getSeedSource());
					gpEntry.setCross(gpListData.getGroupName());
					listDetails.addGermplasmEntry(gpEntry);
				}
			}
			return listDetails;
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException(GermplasmListServiceImpl.ERROR_NAME, e);
		}
	}

	@Override
	public List<GermplasmListSummary> list() {
		List<GermplasmList> allGermplasmLists;
		try {
			allGermplasmLists = this.germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE);
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException(GermplasmListServiceImpl.ERROR_NAME, e);
		}
		return this.mapResults(allGermplasmLists);
	}

}
