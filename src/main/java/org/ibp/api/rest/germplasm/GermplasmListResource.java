
package org.ibp.api.rest.germplasm;

import java.util.List;

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
	public ResponseEntity<List<GermplasmListSummary>> searchGermplasmLists(@PathVariable final String cropname,
			@RequestParam final String q, @RequestParam final String programUUID) {
		final List<GermplasmListSummary> searchResults = this.germplasmListService.searchGermplasmLists(q, programUUID);
		this.populateResourceLinkURLs(searchResults, cropname);
		return new ResponseEntity<List<GermplasmListSummary>>(searchResults, HttpStatus.OK);
	}

	@ApiOperation(value = "List germplasm lists.", notes = "List germplasm lists.")
	@RequestMapping(value = "/{cropname}/list", method = RequestMethod.GET)
	public ResponseEntity<List<GermplasmListSummary>> list(@PathVariable final String cropname) {
		final List<GermplasmListSummary> germplasmLists = this.germplasmListService.list();
		this.populateResourceLinkURLs(germplasmLists, cropname);
		return new ResponseEntity<List<GermplasmListSummary>>(germplasmLists, HttpStatus.OK);
	}

	@ApiOperation(value = "Get germplasm list details by list id.", notes = "Get germplasm list details by list id.")
	@RequestMapping(value = "/{cropname}/{listId}", method = RequestMethod.GET)
	public ResponseEntity<GermplasmListDetails> getGermplasmListDetails(@PathVariable final String cropname,
			@PathVariable final Integer listId) {
		return new ResponseEntity<GermplasmListDetails>(this.germplasmListService.getGermplasmListDetails(listId), HttpStatus.OK);
	}

	private void populateResourceLinkURLs(final List<GermplasmListSummary> summariesList, final String cropName) {
		if (summariesList != null && !summariesList.isEmpty()) {
			for (final GermplasmListSummary summary : summariesList) {
				summary.setListDetailsUrl(this.resourceURLLinkProvider.getGermplasmListDetailsUrl(summary.getListId(), cropName));
			}
		}
	}

}
