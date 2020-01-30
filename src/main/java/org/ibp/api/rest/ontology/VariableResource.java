
package org.ibp.api.rest.ontology;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.java.ontology.VariableService;
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
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Variable Service")
@Controller
@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
@RequestMapping("/crops")
public class VariableResource {

	@Autowired
	private VariableService variableService;

	@ApiOperation(value = "All variables", notes = "Gets all variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableDetails>> listAllVariables(@PathVariable final String cropname, @RequestParam(value = "property",
			required = false) final String propertyId, @RequestParam(value = "favourite", required = false) final Boolean favourite, @RequestParam final String programUUID) {
		return new ResponseEntity<>(this.variableService.getAllVariablesByFilter(cropname, programUUID, propertyId, favourite), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Variable", notes = "Get Variable By Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<VariableDetails> getVariableById(@PathVariable final String cropname,
			@RequestParam final String programUUID, @PathVariable final String id) {
		return new ResponseEntity<>(this.variableService.getVariableById(cropname, programUUID, id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Variable", notes = "Add new variable using given data")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addVariable(@PathVariable final String cropname, @RequestParam final String programUUID,
			@RequestBody final VariableDetails variable) {
		return new ResponseEntity<>(this.variableService.addVariable(cropname, programUUID, variable), HttpStatus.CREATED);
	}

	/**
	 *
	 * @param cropname The name of the crop which is we wish to add variable.
	 * @param programUUID programUUID to which variable is related
	 * @param id variable id
	 * @param variable the variable data to update with.
	 */
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Variable", notes = "Update variable using given data")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateVariable(@PathVariable final String cropname, final @RequestParam String programUUID,
			@PathVariable final String id, @RequestBody final VariableDetails variable) {
		this.variableService.updateVariable(cropname, programUUID, id, variable);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Variable", notes = "Delete Variable by Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteVariable(@PathVariable final String cropname, @RequestParam final String programUUID, @PathVariable
	final String id) {
		this.variableService.deleteVariable(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
