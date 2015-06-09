
package org.ibp.api.rest.ontology;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Variable Service")
@Controller
@RequestMapping("/ontology")
public class VariableResource {

	@Autowired
	private VariableService variableService;

	@ApiOperation(value = "All variables", notes = "Gets all variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableSummary>> listAllVariables(@PathVariable String cropname, @RequestParam(value = "property",
			required = false) String propertyId, @RequestParam(value = "favourite", required = false) Boolean favourite, @RequestParam(
			value = "programId") String programId) {
		return new ResponseEntity<>(this.variableService.getAllVariablesByFilter(cropname, programId, propertyId, favourite), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Variable", notes = "Get Variable By Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<VariableDetails> getVariableById(@PathVariable String cropname,
			@RequestParam(value = "programId") String programId, @PathVariable String id) {
		return new ResponseEntity<>(this.variableService.getVariableById(cropname, programId, id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Variable", notes = "Add new variable using given data")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addVariable(@PathVariable String cropname, @RequestParam(value = "programId") String programId,
			@RequestBody VariableSummary variable) {
		return new ResponseEntity<>(this.variableService.addVariable(cropname, programId, variable), HttpStatus.CREATED);
	}

	/**
	 *
	 * @param cropname The name of the crop which is we wish to add variable.
	 * @param programId programId to which variable is related
	 * @param id variable id
	 * @param variable the variable data to update with.
	 */
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Variable", notes = "Update variable using given data")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateVariable(@PathVariable String cropname, @RequestParam(value = "programId") String programId,
			@PathVariable String id, @RequestBody VariableSummary variable) {
		this.variableService.updateVariable(cropname, programId, id, variable);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Variable", notes = "Delete Variable by Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteVariable(@PathVariable String cropname, @PathVariable String id) {
		this.variableService.deleteVariable(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
