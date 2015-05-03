
package org.ibp.api.rest.germplasm;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListSummary;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.rest.ResourceURLLinkProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Germplasm List Services")
@RestController
@RequestMapping(GermplasmListResource.URL)
public class GermplasmListResource {
	
	public static final String URL = "/germplasmList";

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private ResourceURLLinkProvider resourceURLLinkProvider;

	@ApiOperation(value = "Search germplasm lists.", notes = "Search germplasm lists.")
	@RequestMapping(value = "/{cropname}/search", method = RequestMethod.GET)
	public ResponseEntity<List<GermplasmListSummary>> searchGermplasmLists(@PathVariable String cropname, @RequestParam String q) throws MiddlewareQueryException {
		List<GermplasmListSummary> searchResults = germplasmListService.searchGermplasmLists(q);
		populateResourceLinkURLs(searchResults, cropname);
		return new ResponseEntity<List<GermplasmListSummary>>(searchResults, HttpStatus.OK);
	}

	@ApiOperation(value = "Get all germplasm lists.", notes = "Get all germplasm lists.")
	@RequestMapping(value = "/{cropname}/all", method = RequestMethod.GET)
	public ResponseEntity<List<GermplasmListSummary>> getAllGermplasmLists(@PathVariable String cropname) throws MiddlewareQueryException {
		List<GermplasmListSummary> allGermplasmLists = germplasmListService.getAllGermplasmLists();
		populateResourceLinkURLs(allGermplasmLists, cropname);
		return new ResponseEntity<List<GermplasmListSummary>>(allGermplasmLists, HttpStatus.OK);
	}

	@ApiOperation(value = "Get germplasm list details by list id.", notes = "Get germplasm list details by list id.")
	@RequestMapping(value = "/{cropname}/{listId}", method = RequestMethod.GET)
	public ResponseEntity<GermplasmListDetails> getGermplasmListDetails(@PathVariable String cropname, @PathVariable Integer listId) throws MiddlewareQueryException {
		return new ResponseEntity<GermplasmListDetails>(germplasmListService.getGermplasmListDetails(listId), HttpStatus.OK);
	}

	private void populateResourceLinkURLs(List<GermplasmListSummary> summariesList, String cropName) {
		if (summariesList != null && !summariesList.isEmpty()) {
			for (GermplasmListSummary summary : summariesList) {
				summary.setListDetailsUrl(resourceURLLinkProvider.getGermplasmListDetailsUrl(summary.getListId(), cropName));
			}
		}
	}

}
