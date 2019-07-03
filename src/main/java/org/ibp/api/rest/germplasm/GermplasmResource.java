
package org.ibp.api.rest.germplasm;

import java.util.List;

import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.ResourceURLLinkProvider;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
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
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "Germplasm Services")
@RestController
@RequestMapping(GermplasmResource.URL)
public class GermplasmResource {

	public static final String URL = "/germplasm";

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private ResourceURLLinkProvider resourceURLLinkProvider;

	@ApiOperation(value = "Search germplasm.", notes = "Search germplasm.")
	@RequestMapping(value = "/{cropname}/search", method = RequestMethod.GET)
	public ResponseEntity<PagedResult<GermplasmSummary>> searchGermplasm(@PathVariable final String cropname, @RequestParam final String q,
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.",
					required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@ApiParam(value = "Number of results to retrieve per page. Defaults to " + PagedResult.DEFAULT_PAGE_SIZE + " if not supplied. Max page size allowed is 10000.",
					required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize) {

		PagedResult<GermplasmSummary> result = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<GermplasmSummary>() {

			@Override
			public long getCount() {
				return GermplasmResource.this.germplasmService.searchGermplasmCount(q);
			}

			@Override
			public List<GermplasmSummary> getResults(PagedResult<GermplasmSummary> pagedResult) {
				final List<GermplasmSummary> pageResults = GermplasmResource.this.germplasmService.searchGermplasm(q, pagedResult.getPageNumber(),
						pagedResult.getPageSize());
				for (GermplasmSummary summary : pageResults) {
					GermplasmResource.this.populateParentLinkUrls(cropname, summary);
				}
				return pageResults;
			}
		});
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private void populateParentLinkUrls(String cropname, GermplasmSummary summary) {
		if (summary.getParent1Id() != null && !summary.getParent1Id().equals("Unknown")) {
			summary.setParent1Url(this.resourceURLLinkProvider.getGermplasmByIDUrl(summary.getParent1Id(), cropname));
		} else {
			summary.setParent1Url("Not applicable as parent 1 is is unknown.");
		}

		if (summary.getParent2Id() != null && !summary.getParent2Id().equals("Unknown")) {
			summary.setParent2Url(this.resourceURLLinkProvider.getGermplasmByIDUrl(summary.getParent2Id(), cropname));
		} else {
			summary.setParent2Url("Not applicable as parent 2 is is unknown.");
		}
	}

	@ApiOperation(value = "Get germplasm by germplasm id.", notes = "Get germplasm by germplasm id.")
	@RequestMapping(value = "/{cropname}/{germplasmId}", method = RequestMethod.GET)
	public ResponseEntity<GermplasmSummary> getGermplasm(@PathVariable String cropname, @PathVariable String germplasmId) {
		GermplasmSummary summary = this.germplasmService.getGermplasm(germplasmId);
		if (summary != null) {
			this.populateParentLinkUrls(cropname, summary);
			return new ResponseEntity<GermplasmSummary>(summary, HttpStatus.OK);
		} else {
			throw new ApiRuntimeException("No germplasm record found for the supplied identifier: " + germplasmId);
		}
	}

	@ApiOperation(value = "Get germplasm pedigree tree by germplasm id.", notes = "Get germplasm pedigree tree by germplasm id.")
	@RequestMapping(value = "/{cropname}/pedigree/{germplasmId}", method = RequestMethod.GET)
	public ResponseEntity<PedigreeTree> getPedigreeTree(
			@PathVariable String cropname, 
			@PathVariable String germplasmId,
			
			@RequestParam(required = false) 
			@ApiParam(name = "levels", value = "Optional. If not specified default number of levels are 20.") 
			Integer levels) {
		return new ResponseEntity<PedigreeTree>(this.germplasmService.getPedigreeTree(germplasmId, levels), HttpStatus.OK);
	}

	@ApiOperation(value = "Get germplasm descendant tree by germplasm id.", notes = "Get germplasm descendant tree by germplasm id.")
	@RequestMapping(value = "/{cropname}/descendants/{germplasmId}", method = RequestMethod.GET)
	public ResponseEntity<DescendantTree> getDescendantTree(@PathVariable String cropname, @PathVariable String germplasmId) {
		return new ResponseEntity<DescendantTree>(this.germplasmService.getDescendantTree(germplasmId), HttpStatus.OK);
	}
}
