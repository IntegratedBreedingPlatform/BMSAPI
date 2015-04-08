package org.ibp.api.rest.ontology;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.MethodRequestValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.OntologyMethodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
public class OntologyMethodResource {

	@Autowired
	private CropNameValidator cropNameValidator;

	@Autowired
	private RequestIdValidator requestIdValidator;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private MethodRequestValidator methodRequestValidator;

	@Autowired
	private TermDeletableValidator termDeletableValidator;

	@Autowired
	private OntologyMethodService ontologyMethodService;

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	@ApiOperation(value = "All Methods", notes = "Get all methods")
	@RequestMapping(value = "/{cropname}/methods", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodSummary>> listAllMethods(@PathVariable String cropname) throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		List<MethodSummary> methodList = this.ontologyMethodService.getAllMethods();
		return new ResponseEntity<>(methodList, HttpStatus.OK);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	@ApiOperation(value = "Get method by id", notes = "Get method using given method id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<MethodResponse> getMethodById(@PathVariable String cropname, @PathVariable String id) throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		TermRequest request = new TermRequest(Integer.valueOf(id), "method", CvId.METHODS.getId());
		this.termValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		return new ResponseEntity<>(this.ontologyMethodService.getMethod(Integer.valueOf(id)), HttpStatus.OK);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@ApiOperation(value = "Add Method", notes = "Add a Method using Given Data")
	@RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addMethod(@PathVariable String cropname, @RequestBody MethodRequest request, BindingResult bindingResult) throws MiddlewareQueryException {

		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.methodRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyMethodService.addMethod(request), HttpStatus.CREATED);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Method", notes = "Update Method using Given Data")
	@RequestMapping(value = "/{cropname}/methods/{id:.+}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateMethod(@PathVariable String cropname, @PathVariable String id, @RequestBody MethodRequest request, BindingResult bindingResult) throws MiddlewareQueryException, MiddlewareException {

		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		request.setId(Integer.valueOf(id));
		this.methodRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyMethodService.updateMethod(request.getId(), request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Method", notes = "Delete Method using Given Id")
	@RequestMapping(value = "/{cropname}/methods/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteMethod(@PathVariable String cropname, @PathVariable String id) throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.termDeletableValidator.validate(new TermRequest(Integer.valueOf(id), "method", CvId.METHODS.getId()), bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyMethodService.deleteMethod(Integer.valueOf(id));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
