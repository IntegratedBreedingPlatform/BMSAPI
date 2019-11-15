package org.ibp.api.rest.ontology;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.ibp.api.Util;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * This is temporary resource to test get variables using types of filter
 */

@Api(value = "Ontology Variable Filter Service")
@Controller
@RequestMapping("/ontology")
public class VariableFilterResource {

	@Autowired
	private VariableService variableService;

	@ApiOperation(value = "All variables using given filter", notes = "Gets all variables using filter")
	@RequestMapping(value = "/{cropname}/filtervariables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableDetails>> listAllVariablesUsingFilter(
			@ApiParam(value = "Use <code>GET /crop/list</code> service to retrieve possible crop name values that can be supplied here.", required = true)
			@PathVariable String cropname,

			@ApiParam(value = "Use <code>GET /program/list</code> service to retrieve program uuid that can be supplied here.")
			@RequestParam(value = "programId") String programId,

			@ApiParam(value = "Use <code>GET /ontology/{cropname}/properties</code> service "
					+ " to retrieve possible property ids that can be supplied here as a comma separated list.")
			@RequestParam(value = "propertyIds", required = false) Set<Integer> propertyIds,

			@ApiParam(value = "Use <code>GET /ontology/{cropname}/methods</code> service "
					+ " to retrieve possible method ids that can be supplied here as a comma separated list.")
			@RequestParam(value = "methodIds", required = false) Set<Integer> methodIds,

			@ApiParam(value = "Use <code>GET /ontology/{cropname}/scales</code> service "
					+ " to retrieve possible scale ids that can be supplied here as a comma separated list.")
			@RequestParam(value = "scaleIds", required = false) Set<Integer> scaleIds,

			@ApiParam(value = "Use <code>GET /ontology/{cropname}/variables</code> service "
					+ " to retrieve possible variable ids that can be supplied here as a comma separated list.")
			@RequestParam(value = "variableIds", required = false) Set<Integer> variableIds,

			@ApiParam(value = "Specify ids of variables to exclude."
					+ " Use <code>GET /ontology/{cropname}/variables</code> service"
					+ " to retrieve possible variable ids that can be supplied here as a comma separated list.")
			@RequestParam(value = "exclusionVariableIds", required = false) Set<Integer> exclusionVariableIds,

			@ApiParam(value = "Use <code>GET /ontology/datatypes</code> service "
					+ " to retrieve possible data type ids that can be supplied here as a comma separated list.")
			@RequestParam(value = "dataTypeIds", required = false) Set<Integer> dataTypeIds,

			@ApiParam(value = "Use <code>GET /ontology/variableTypes</code> service "
					+ " to list possible variable type ids that can be supplied here as a comma separated list. ")
			@RequestParam(value = "variableTypeIds", required = false) Set<Integer> variableTypeIds,

			@ApiParam(value = "Use <code>GET /ontology/{cropname}/classes</code> service "
					+ " to retrieve possible property class values that can be supplied here as a comma separated list.")
			@RequestParam(value = "propertyClasses", required = false) Set<String> propertyClasses) {

		VariableFilter variableFilter = new VariableFilter();
		variableFilter.setProgramUuid(programId);

		if(!Util.isNullOrEmpty(propertyIds)){
			for(Integer i : propertyIds){
				variableFilter.addPropertyId(i);
			}
		}

		if(!Util.isNullOrEmpty(methodIds)){
			for(Integer i : methodIds){
				variableFilter.addMethodId(i);
			}
		}

		if(!Util.isNullOrEmpty(scaleIds)){
			for(Integer i : scaleIds){
				variableFilter.addScaleId(i);
			}
		}

		if(!Util.isNullOrEmpty(variableIds)){
			for(Integer i : variableIds){
				variableFilter.addVariableId(i);
			}
		}

		if(!Util.isNullOrEmpty(exclusionVariableIds)){
			for(Integer i : exclusionVariableIds){
				variableFilter.addExcludedVariableId(i);
			}
		}

		if(!Util.isNullOrEmpty(dataTypeIds)){
			for(Integer i : dataTypeIds){
				variableFilter.addDataType(i);
			}
		}

		if(!Util.isNullOrEmpty(variableTypeIds)){
			for(Integer i : variableTypeIds){
				variableFilter.addVariableType(i);
			}
		}

		if(!Util.isNullOrEmpty(propertyClasses)){
			for(String s : propertyClasses){
				variableFilter.addPropertyClass(s);
			}
		}

		return new ResponseEntity<>(this.variableService.getVariablesByFilter(cropname, programId, variableFilter), HttpStatus.OK);
	}

}
