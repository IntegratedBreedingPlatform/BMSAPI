
package org.ibp.api.rest.germplasm;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListSummary;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/germplasm")
public class GermplasmResource {

	@Autowired
	private GermplasmService germplasmService;

	@RequestMapping(value = "/{cropname}/list/search", method = RequestMethod.GET)
	public ResponseEntity<List<GermplasmListSummary>> searchGermplasmLists(@PathVariable String cropname, @RequestParam String q) throws MiddlewareQueryException {
		return new ResponseEntity<List<GermplasmListSummary>>(germplasmService.searchGermplasmLists(q), HttpStatus.OK);
	}

	@RequestMapping(value = "/{cropname}/list/{listId}", method = RequestMethod.GET)
	public ResponseEntity<GermplasmListDetails> getGermplasmListDetails(@PathVariable String cropname, @PathVariable Integer listId) throws MiddlewareQueryException {
		return new ResponseEntity<GermplasmListDetails>(germplasmService.getGermplasmListDetails(listId), HttpStatus.OK);
	}

}
