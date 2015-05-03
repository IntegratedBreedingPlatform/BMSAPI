
package org.ibp.api.java.impl.middleware.germplasm;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListSummary;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GermplasmServiceImpl implements GermplasmService {

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Override
	public List<GermplasmListSummary> searchGermplasmLists(String searchText) {

		List<GermplasmListSummary> results = new ArrayList<GermplasmListSummary>();
		List<GermplasmList> matchingLists;
		try {
			matchingLists = this.germplasmListManager.searchForGermplasmList(searchText, Operation.LIKE);

			if (matchingLists != null && !matchingLists.isEmpty()) {
				for (GermplasmList gpList : matchingLists) {
					GermplasmListSummary res = new GermplasmListSummary();
					res.setListId(gpList.getId());
					res.setListName(gpList.getName());
					res.setDescription(gpList.getDescription());
					res.setNotes(gpList.getNotes());
					res.setListSize(gpList.getListData().size());
					results.add(res);
				}
			}
			return results;
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
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
					GermplasmSummary gp = new GermplasmSummary();
					gp.setGid(gpListData.getGid());
					gp.setDesignation(gpListData.getDesignation());
					gp.setEntryCode(gpListData.getEntryCode());
					gp.setSource(gpListData.getSeedSource());
					gp.setGroupName(gpListData.getGroupName());
					listDetails.addGermplasmEntry(gp);
				}
			}
			return listDetails;
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

}
