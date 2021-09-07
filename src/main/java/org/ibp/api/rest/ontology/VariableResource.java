
package org.ibp.api.rest.ontology;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.Util;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
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

import java.util.List;
import java.util.Set;


@Api(value = "Ontology Variable Service")
@Controller
@RequestMapping("/crops")
public class VariableResource {

	@Autowired
	private VariableService variableService;

	@ApiOperation(value = "All variables", notes = "Gets all variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
	public ResponseEntity<List<VariableDetails>> listAllVariables(@PathVariable final String cropname, @RequestParam(value = "property",
			required = false) final String propertyId, @RequestParam(value = "favourite", required = false) final Boolean favourite, @RequestParam final String programUUID) {
		return new ResponseEntity<>(this.variableService.getAllVariablesByFilter(cropname, programUUID, propertyId, favourite), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Variable", notes = "Get Variable By Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')" + PermissionsEnum.HAS_MANAGE_GERMPLASM)
	public ResponseEntity<VariableDetails> getVariableById(@PathVariable final String cropname,
			@RequestParam final String programUUID, @PathVariable final String id) {
		return new ResponseEntity<>(this.variableService.getVariableById(cropname, programUUID, id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Variable", notes = "Add new variable using given data")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
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
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
	public ResponseEntity updateVariable(@PathVariable final String cropname, final @RequestParam String programUUID,
			@PathVariable final String id, @RequestBody final VariableDetails variable) {
		this.variableService.updateVariable(cropname, programUUID, id, variable);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Variable", notes = "Delete Variable by Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
	public ResponseEntity deleteVariable(@PathVariable final String cropname, @RequestParam final String programUUID, @PathVariable
	final String id) {
		this.variableService.deleteVariable(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "All variables using given filter", notes = "Gets all variables using filter")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'MANAGE_ONTOLOGIES', 'CROP_MANAGEMENT')" + PermissionsEnum.HAS_MANAGE_GERMPLASM)
	@RequestMapping(value = "/{cropname}/variables/filter", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableDetails>> listAllVariablesUsingFilter(
		@ApiParam(value = "Use <code>GET /crop/list</code> service to retrieve possible crop name values that can be supplied here.", required = true)
		@PathVariable final String cropname,

		@ApiParam(value = "Use <code>GET /program/list</code> service to retrieve program uuid that can be supplied here.", required = true)
		@RequestParam(value = "programUUID") final String programUUID,

		@ApiParam(value = "Use <code>GET /ontology/{cropname}/properties</code> service "
			+ " to retrieve possible property ids that can be supplied here as a comma separated list.")
		@RequestParam(value = "propertyIds", required = false) final Set<Integer> propertyIds,

		@ApiParam(value = "Use <code>GET /ontology/{cropname}/methods</code> service "
			+ " to retrieve possible method ids that can be supplied here as a comma separated list.")
		@RequestParam(value = "methodIds", required = false) final Set<Integer> methodIds,

		@ApiParam(value = "Use <code>GET /ontology/{cropname}/scales</code> service "
			+ " to retrieve possible scale ids that can be supplied here as a comma separated list.")
		@RequestParam(value = "scaleIds", required = false) final Set<Integer> scaleIds,

		@ApiParam(value = "Use <code>GET /ontology/{cropname}/variables</code> service "
			+ " to retrieve possible variable ids that can be supplied here as a comma separated list.")
		@RequestParam(value = "variableIds", required = false) final Set<Integer> variableIds,

		@ApiParam(value = "Specify ids of variables to exclude."
			+ " Use <code>GET /ontology/{cropname}/variables</code> service"
			+ " to retrieve possible variable ids that can be supplied here as a comma separated list.")
		@RequestParam(value = "exclusionVariableIds", required = false) final Set<Integer> exclusionVariableIds,

		@ApiParam(value = "Use <code>GET /ontology/datatypes</code> service "
			+ " to retrieve possible data type ids that can be supplied here as a comma separated list.")
		@RequestParam(value = "dataTypeIds", required = false) final Set<Integer> dataTypeIds,

		@ApiParam(value = "Use <code>GET /ontology/variableTypes</code> service "
			+ " to list possible variable type ids that can be supplied here as a comma separated list. ")
		@RequestParam(value = "variableTypeIds", required = false) final Set<Integer> variableTypeIds,

		@ApiParam(value = "List of names or alias ")
		@RequestParam(value = "variableNames", required = false) final Set<String> variableNames,

		@ApiParam(value = "Use <code>GET /ontology/{cropname}/classes</code> service "
			+ " to retrieve possible property class values that can be supplied here as a comma separated list.")
		@RequestParam(value = "propertyClasses", required = false) final Set<String> propertyClasses,

		@RequestParam(required = false) final Set<Integer> datasetIds
	) {

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.setProgramUuid(programUUID);

		if (!Util.isNullOrEmpty(propertyIds)) {
			propertyIds.forEach(variableFilter::addPropertyId);
		}

		if (!Util.isNullOrEmpty(methodIds)) {
			methodIds.forEach(variableFilter::addMethodId);
		}

		if (!Util.isNullOrEmpty(scaleIds)) {
			scaleIds.forEach(variableFilter::addScaleId);
		}

		if (!Util.isNullOrEmpty(variableIds)) {
			variableIds.forEach(variableFilter::addVariableId);
		}

		if (!Util.isNullOrEmpty(exclusionVariableIds)) {
			exclusionVariableIds.forEach(variableFilter::addExcludedVariableId);
		}

		if (!Util.isNullOrEmpty(dataTypeIds)) {
			dataTypeIds.forEach(variableFilter::addDataType);
		}

		if (!Util.isNullOrEmpty(variableTypeIds)) {
			variableTypeIds.forEach(variableFilter::addVariableType);
		}

		if (!Util.isNullOrEmpty(propertyClasses)) {
			propertyClasses.forEach(variableFilter::addPropertyClass);
		}

		if (!Util.isNullOrEmpty(variableNames)) {
			variableNames.forEach(variableFilter::addName);
		}

		if (!Util.isNullOrEmpty(datasetIds)) {
			datasetIds.forEach(variableFilter::addDatasetId);
		}

		return new ResponseEntity<>(this.variableService.getVariablesByFilter(cropname, programUUID, variableFilter), HttpStatus.OK);
	}

}
