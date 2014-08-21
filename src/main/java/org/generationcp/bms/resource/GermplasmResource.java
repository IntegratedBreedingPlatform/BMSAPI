package org.generationcp.bms.resource;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.GermplasmListDetails;
import org.generationcp.bms.domain.GermplasmListSummary;
import org.generationcp.bms.domain.GermplasmSearchResult;
import org.generationcp.bms.domain.GermplasmSummary;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.bms.util.Utils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/germplasm")
public class GermplasmResource {

	@Autowired
	private SimpleDao simpleDao;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	
	@RequestMapping(value="/search", method = RequestMethod.GET)
	public List<GermplasmSearchResult> search(@RequestParam String q) {
		return simpleDao.searchGermplasm(q);
	}
	
	@RequestMapping(value = "/list/search", method = RequestMethod.GET)
	public List<GermplasmListSummary> searchGermplasmLists(
			@RequestParam String q, HttpServletRequest httpRequest)
			throws MiddlewareQueryException {
		
		List<GermplasmListSummary> results = new ArrayList<GermplasmListSummary>();
		List<GermplasmList> matchingLists = this.germplasmListManager.searchForGermplasmList(q, Operation.LIKE, true);			
		
		if(matchingLists != null && !matchingLists.isEmpty()) {
			for(GermplasmList gpList : matchingLists) {
				GermplasmListSummary res = new GermplasmListSummary();
				res.setListId(gpList.getId());
				res.setListName(gpList.getName());
				res.setDescription(gpList.getDescription());
				res.setNotes(gpList.getNotes());
				res.setListSize(gpList.getListData().size());			
				String baseUrl = Utils.getBaseUrl(httpRequest);			
				res.setListDataUrl(String.format("%s/germplasm/list/%s", baseUrl, gpList.getId()));
				results.add(res);			
			}		
		}
		return results;
	}
	
	@RequestMapping(value = "/list/{listId}", method = RequestMethod.GET)
	public GermplasmListDetails getGermplasmListDetails(
			@PathVariable Integer listId,
			HttpServletRequest httpRequest) throws MiddlewareQueryException {
		
		GermplasmListDetails listDetails = new GermplasmListDetails();
		GermplasmList gpList = this.germplasmListManager.getGermplasmListById(listId);
		
		if(gpList != null) {
			listDetails.setListId(gpList.getId());
			listDetails.setListName(gpList.getName());
			listDetails.setDescription(gpList.getDescription());
			listDetails.setNotes(gpList.getNotes());
			listDetails.setListSize(gpList.getListData().size());		
			String baseUrl = Utils.getBaseUrl(httpRequest);			
			listDetails.setListDataUrl(String.format("%s/germplasm/list/%s", baseUrl, gpList.getId()));
			
			for(GermplasmListData gpListData : gpList.getListData()) {
				GermplasmSummary gp = new GermplasmSummary();
				gp.setGid(gpListData.getGid());
				gp.setDesignation(gpListData.getDesignation());
				gp.setEntryCode(gpListData.getEntryCode());
				gp.setSource(gpListData.getSeedSource());
				gp.setGroupName(gpListData.getGroupName());
				listDetails.addGermplasmEntry(gp);
			}		
			return listDetails;
		}
		throw new NotFoundException();
	}
	
}
