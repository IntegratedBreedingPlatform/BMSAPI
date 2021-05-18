package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.germplasm.pedigree.GermplasmNeighborhoodNode;
import org.generationcp.middleware.api.germplasm.pedigree.GermplasmTreeNode;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.ibp.api.java.germplasm.GermplasmPedigreeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Germplasm Pedigree Services")
@Controller
public class GermplasmPedigreeResource {

	@Resource
	private GermplasmPedigreeService germplasmPedigreeService;

	@ApiOperation(value = "Returns the of the given germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/tree", method = RequestMethod.GET)
	@ResponseBody
	public GermplasmTreeNode getGermplasmPedigreeTree(@PathVariable final String cropName, @PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Integer level,
		@RequestParam final boolean includeDerivativeLines) {
		return this.germplasmPedigreeService.getGermplasmPedigreeTree(gid, level, includeDerivativeLines);
	}

	@ApiOperation(value = "Returns the generation history of the given germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/generation-history", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmDto>> getGenerationHistory(
		@PathVariable final String cropName, @PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmPedigreeService.getGenerationHistory(gid), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns the management neighbors of the given germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/management-neighbors", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmDto>> getManagementNeighbors(
		@PathVariable final String cropName, @PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmPedigreeService.getManagementNeighbors(gid), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns the group relatives of the given germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/group-relatives", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmDto>> getGroupRelatives(
		@PathVariable final String cropName, @PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmPedigreeService.getGroupRelatives(gid), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns the Maintenance neighborhood of the given germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/maintenance-neighbors", method = RequestMethod.GET)
	@ResponseBody
	public GermplasmNeighborhoodNode getMaintenanceNeighborhood(
		@PathVariable final String cropName, @PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final Integer numberOfStepsBackward,
		@RequestParam final Integer numberOfStepsForward) {
		return this.germplasmPedigreeService.getGermplasmMaintenanceNeighborhood(gid, numberOfStepsBackward, numberOfStepsForward);
	}

	@ApiOperation(value = "Returns the Derivative neighborhood of the given germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/derivative-neighbors", method = RequestMethod.GET)
	@ResponseBody
	public GermplasmNeighborhoodNode getDerivativeNeighborhood(
		@PathVariable final String cropName, @PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final Integer numberOfStepsBackward,
		@RequestParam final Integer numberOfStepsForward) {
		return this.germplasmPedigreeService.getGermplasmDerivativeNeighborhood(gid, numberOfStepsBackward, numberOfStepsForward);
	}
}
