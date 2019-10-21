package org.ibp.api.rest.germplasmlist;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Germplasm Services")
@Controller
@RequestMapping("/germplasmLists")
public class GermplasmListResource {

	// FIXME externalize
	public static final String ERROR = "ERROR";

	@Autowired
	public GermplamListService germplamListService;

	@Autowired
	public ContextUtil contextUtil;

	@ApiOperation(value = "Search Germplasm List", notes = "Search Germplasm List")
	@RequestMapping(value = "/{crop}/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmList>> search(
		@PathVariable final String crop,
		@ApiParam("Only return the exact match of the search text") @RequestParam final boolean exactMatch,
		@ApiParam("The name of the list to be searched") @RequestParam final String searchString, final Pageable pageable) {
		final List<GermplasmList> germplasmLists =
			this.germplamListService.search(searchString, exactMatch, this.contextUtil.getCurrentProgramUUID(), pageable);
		return new ResponseEntity<>(germplasmLists, HttpStatus.OK);
	}
}
