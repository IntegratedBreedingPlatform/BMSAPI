package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.germplasm.pedigree.GermplasmTreeNode;
import org.ibp.api.java.germplasm.GermplasmPedigreeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Api(value = "Germplasm Pedigree Services")
@Controller
public class GermplasmPedigreeResource {

	@Resource
	private GermplasmPedigreeService germplasmPedigreeService;


	@ApiOperation(value = "Get Germplasm Pedigree Tree")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/tree/{level}", method = RequestMethod.GET)
	@ResponseBody
	public GermplasmTreeNode getGermplasmPedigreeTree(@PathVariable final String cropName, @PathVariable final Integer gid,
		@PathVariable final Integer level,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final boolean includeDerivativeLines) {
		return this.germplasmPedigreeService.getGermplasmPedigreeTree(gid, level, includeDerivativeLines);
	}
}
