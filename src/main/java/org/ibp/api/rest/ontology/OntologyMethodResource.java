package org.ibp.api.rest.ontology;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodRequest;
import org.ibp.api.domain.ontology.MethodRequestBase;
import org.ibp.api.domain.ontology.MethodResponse;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.OntologyMapper;
import org.ibp.api.java.impl.middleware.ontology.validator.MethodRequestValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.OntologyMethodService;
import org.ibp.api.rest.AbstractResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
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
public class OntologyMethodResource extends AbstractResource {

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
	public ResponseEntity<MethodResponse> getMethodById(@PathVariable String cropname, @PathVariable String id)  {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		TermRequest request = new TermRequest(id, "method", CvId.METHODS.getId());
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
	@ApiOperation(value = "Add Method", notes = "Add a new Method")
	@RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addMethod(@PathVariable String cropname, @RequestBody MethodRequestBase requestBase)  {
	  ModelMapper mapper = OntologyMapper.getInstance();
	  MethodRequest request = mapper.map(requestBase, MethodRequest.class);
	  BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
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
	@ApiOperation(value = "Update Method", notes = "Update a Method by Id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateMethod(@PathVariable String cropname, @PathVariable String id, @RequestBody MethodRequestBase requestBase)  {
	  ModelMapper mapper = OntologyMapper.getInstance();
	  MethodRequest request = mapper.map(requestBase, MethodRequest.class);
	  BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		request.setId(id);
		this.methodRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyMethodService.updateMethod(Integer.valueOf(request.getId()), request);
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
	public ResponseEntity deleteMethod(@PathVariable String cropname, @PathVariable String id)  {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.termDeletableValidator.validate(new TermRequest(id, "Method", CvId.METHODS.getId()), bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyMethodService.deleteMethod(Integer.valueOf(id));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
