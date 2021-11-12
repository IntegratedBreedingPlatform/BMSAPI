package org.ibp.api.rest.germplasmlist;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.germplasmlist.GermplasmListVariableRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.ibp.api.java.germplasm.GermplasmListVariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Api(value = "Germplasm List Variables Services")
@Controller
public class GermplasmListVariableResource {

	@Autowired
	public GermplasmListVariableService germplasmListVariableService;

	@ApiOperation(value = "Add a variable to the list", notes = "Add a variable to the list")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS', 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/variables", method = RequestMethod.PUT)
	public ResponseEntity<Void> addVariable(
		@PathVariable final String cropName, @PathVariable final Integer listId, @RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListVariableRequestDto germplasmListVariableRequestDto) {
		this.germplasmListVariableService.addVariableToList(listId, germplasmListVariableRequestDto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Remove list variables", notes = "Remove a set of variables from a germplasm list")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS', 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/variables", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeVariables(
		@PathVariable final String cropName, @PathVariable final Integer listId,
		@RequestParam(value = "variableIds") final Set<Integer> variableIds,
		@RequestParam(required = false) final String programUUID) {

		this.germplasmListVariableService.removeListVariables(listId, variableIds);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get the variables associated to the list filtered by variableType", notes = "Get the list variables filtered by variableType")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS', 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/variables", method = RequestMethod.GET)
	public ResponseEntity<List<Variable>> getVariables(
		@PathVariable final String cropName, @PathVariable final Integer listId, @RequestParam final Integer variableTypeId,
		@RequestParam(required = false) final String programUUID) {

		final List<org.generationcp.middleware.domain.ontology.Variable> variables =
			this.germplasmListVariableService.getGermplasmListVariables(cropName, programUUID, listId, variableTypeId);
		return new ResponseEntity<>(variables, HttpStatus.OK);
	}

}
