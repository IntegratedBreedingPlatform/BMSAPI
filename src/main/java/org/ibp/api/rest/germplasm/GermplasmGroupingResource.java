package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.GermplasmGroup;
import org.ibp.api.domain.germplasm.GermplasmUngroupingResponse;
import org.ibp.api.java.germplasm.GermplasmGroupingRequest;
import org.ibp.api.java.germplasm.GermplasmGroupingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Germplasm Grouping Services")
@Controller
public class GermplasmGroupingResource {

	@Resource
	private GermplasmGroupingService germplasmGroupingService;

	@ApiOperation(value = "Mark germplasm lines as fixed")
	@RequestMapping(value = "/crops/{cropName}/germplasm/grouping", method = RequestMethod.POST)
	@ResponseBody
	public List<GermplasmGroup> fixLines(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmGroupingRequest germplasmGroupingRequest) {
		return this.germplasmGroupingService.markFixed(germplasmGroupingRequest);
	}

	@ApiOperation(value = "Remove germplasm from whichever grouping they are part of")
	@RequestMapping(value = "/crops/{cropName}/germplasm/ungrouping", method = RequestMethod.POST)
	@ResponseBody
	public GermplasmUngroupingResponse unfixLines(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<Integer> gids) {
		return this.germplasmGroupingService.unfixLines(gids);
	}

}
