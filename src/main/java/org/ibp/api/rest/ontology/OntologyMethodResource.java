package org.ibp.api.rest.ontology;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.java.ontology.OntologyMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
public class OntologyMethodResource {

	@Autowired
	private OntologyMethodService ontologyMethodService;

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	@ApiOperation(value = "All Methods", notes = "Get all methods")
	@RequestMapping(value = "/{cropname}/methods", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodSummary>> listAllMethods(@PathVariable String cropname)  {
		List<MethodSummary> methodList = this.ontologyMethodService.getAllMethods();
		return new ResponseEntity<>(methodList, HttpStatus.OK);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	@ApiOperation(value = "Get method by id", notes = "Get method using method id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<MethodDetails> getMethodById(@PathVariable String cropname, @PathVariable String id)  {
		return new ResponseEntity<>(this.ontologyMethodService.getMethod(id), HttpStatus.OK);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@ApiOperation(value = "Add Method", notes = "Add a new Method")
	@RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addMethod(@PathVariable String cropname, @RequestBody MethodSummary method)  {
		return new ResponseEntity<>(this.ontologyMethodService.addMethod(method), HttpStatus.CREATED);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Method", notes = "Update a Method by Id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateMethod(@PathVariable String cropname, @PathVariable String id, @RequestBody MethodSummary method) {
		this.ontologyMethodService.updateMethod(id, method);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Method", notes = "Delete Method by Id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteMethod(@PathVariable String cropname, @PathVariable String id) {
		this.ontologyMethodService.deleteMethod(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
