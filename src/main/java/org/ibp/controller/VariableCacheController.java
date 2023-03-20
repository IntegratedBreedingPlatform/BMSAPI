
package org.ibp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Variable Cache Controller")
@RestController
@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'MS_STUDY_SETTINGS', 'MS_ADD_STUDY_SETTINGS_VARIABLES', "
	+ " 'MS_GERMPLASM_AND_CHECKS', 'MS_ADD_ENTRY_DETAILS_VARIABLES', "
	+ " 'MS_ENVIRONMENT','MS_ADD_ENVIRONMENTAL_CONDITIONS_VARIABLES', 'MS_ADD_ENVIRONMENT_DETAILS_VARIABLES', "
	+ " 'MS_OBSERVATIONS','MS_ADD_OBSERVATION_TRAIT_VARIABLES', 'MS_ADD_OBSERVATION_SELECTION_VARIABLES', "
	+ " 'CROP_MANAGEMENT', 'MANAGE_ONTOLOGIES')")
public class VariableCacheController {

	@Autowired
	private VariableService variableService;

	@ResponseBody
	@ApiOperation(value = "Delete Variables from VariableCache", notes = "Remove Variables from VariableCache by Ids")
	@RequestMapping(value = "crops/{cropName}/variable-cache/{variablesIds}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteVariablesFromCache(
			@ApiParam(value = "name of the crop", required = true) @PathVariable final String cropName,
			@ApiParam(value = "Comma separated list of variable ids", required = true) @PathVariable final Integer[] variablesIds,
			@RequestParam(value = "programUUID") final String programUUID) {
		this.variableService.deleteVariablesFromCache(cropName, variablesIds, programUUID);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
