package org.ibp.api.rest.germplasm;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.java.germplasm.GermplasmService;
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

@Api(value = "Germplasm Services")
@RestController
@RequestMapping(GermplasmResource.URL)
public class GermplasmResource {

	public static final String URL = "/germplasm";
	
	@Autowired
	private GermplasmService germplasmService;
	
	@ApiOperation(value = "Search germplasm.", notes = "Search germplasm.")
	@RequestMapping(value = "/{cropname}/search", method = RequestMethod.GET)
	public ResponseEntity<List<GermplasmSummary>> searchGermplasm(@PathVariable String cropname, @RequestParam String q) throws MiddlewareQueryException {
		List<GermplasmSummary> searchResults = germplasmService.searchGermplasm(q);
		return new ResponseEntity<List<GermplasmSummary>>(searchResults, HttpStatus.OK);
	}
}
