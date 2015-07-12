package org.ibp.api.rest.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.Util;
import org.ibp.api.domain.ontology.VariableDetails;
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

import java.util.List;
import java.util.Set;

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
			@PathVariable String cropname,
			@RequestParam(value = "propertyIds", required = false) Set<Integer> propertyIds,
			@RequestParam(value = "methodIds", required = false) Set<Integer> methodIds,
			@RequestParam(value = "scaleIds", required = false) Set<Integer> scaleIds,
			@RequestParam(value = "variableIds", required = false) Set<Integer> variableIds,
			@RequestParam(value = "exclusionVariableIds", required = false) Set<Integer> exclusionVariableIds,
			@RequestParam(value = "dataTypeIds", required = false) Set<Integer> dataTypeIds,
			@RequestParam(value = "variableTypeIds", required = false) Set<Integer> variableTypeIds,
			@RequestParam(value = "propertyClasses", required = false) Set<String> propertyClasses,
			@RequestParam(value = "programId") String programId) {

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
				variableFilter.addDataType(DataType.getById(i));
			}
		}

		if(!Util.isNullOrEmpty(variableTypeIds)){
			for(Integer i : variableTypeIds){
				variableFilter.addVariableType(VariableType.getById(i));
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
