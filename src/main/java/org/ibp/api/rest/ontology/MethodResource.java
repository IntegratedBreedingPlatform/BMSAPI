
package org.ibp.api.rest.ontology;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.java.ontology.MethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
public class MethodResource {

	@Autowired
	private MethodService methodService;

	@ApiOperation(value = "All Methods", notes = "Get all methods")
	@RequestMapping(value = "/{cropname}/methods", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodDetails>> listAllMethods(@PathVariable String cropname) {
		return new ResponseEntity<>(this.methodService.getAllMethods(), HttpStatus.OK);
	}

	@ApiOperation(value = "Get method by id", notes = "Get method using method id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<MethodDetails> getMethodById(@PathVariable String cropname, @PathVariable String id) {
		return new ResponseEntity<>(this.methodService.getMethod(id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Method", notes = "Add a new Method")
	@RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addMethod(@PathVariable String cropname, @RequestBody MethodDetails method) {
		return new ResponseEntity<>(this.methodService.addMethod(method), HttpStatus.CREATED);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Method", notes = "Update a Method by Id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateMethod(@PathVariable String cropname, @PathVariable String id, @RequestBody MethodDetails method) {
		this.methodService.updateMethod(id, method);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Method", notes = "Delete Method by Id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteMethod(@PathVariable String cropname, @PathVariable String id) {
		this.methodService.deleteMethod(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
