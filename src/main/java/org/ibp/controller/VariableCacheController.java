
package org.ibp.controller;

import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "Variable Cache Controller")
@Controller
@RequestMapping(value = "/variableCache")
public class VariableCacheController {

	@Autowired
	private VariableService variableService;

	@ResponseBody
	@ApiOperation(value = "Delete Variables from VariableCache", notes = "Remove Variables from VariableCache by Ids")
	@RequestMapping(value = "/{cropName}/{variablesIds}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteVariablesFromCache(
			@ApiParam(value = "name of the crop", required = true) @PathVariable final String cropName,
			@ApiParam(value = "Comma separated list of variable ids", required = true) @PathVariable final Integer[] variablesIds,
			@RequestParam(value = "programId") String programId) {
		this.variableService.deleteVariablesFromCache(cropName, variablesIds, programId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
