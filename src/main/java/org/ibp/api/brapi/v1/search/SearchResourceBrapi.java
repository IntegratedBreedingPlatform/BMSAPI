package org.ibp.api.brapi.v1.search;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.search_request.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.search.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "BrAPI Search Services")
@RestController
public class SearchResourceBrapi {

	@Autowired
	private SearchRequestService searchRequestService;

	@ApiOperation(value = "Post germplasm search", notes = "Post germplasm search")
	@RequestMapping(value = "/{crop}/brapi/v1/search/germplasm", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Integer> postSearchGermplasm(
		@PathVariable final String crop, @RequestBody final GermplasmSearchRequestDto germplasmSearchRequestDto) {
		final SearchRequest searchRequest =
			this.searchRequestService.saveSearchRequest(germplasmSearchRequestDto, GermplasmSearchRequestDto.class);
		return new ResponseEntity<>(searchRequest.getRequestId(), HttpStatus.OK);

	}
}
