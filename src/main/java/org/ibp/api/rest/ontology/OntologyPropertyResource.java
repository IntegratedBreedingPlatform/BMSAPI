package org.ibp.api.rest.ontology;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidator;
import org.ibp.api.java.impl.middleware.ontology.OntologyMapper;
import org.ibp.api.java.impl.middleware.ontology.validator.PropertyRequestValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.OntologyPropertyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")
public class OntologyPropertyResource {

	@Autowired
	private RequestIdValidator requestIdValidator;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private PropertyRequestValidator propertyRequestValidator;

	@Autowired
	private TermDeletableValidator deletableValidator;

	@Autowired
	private OntologyPropertyService ontologyPropertyService;

	@Autowired
	private CropNameValidator cropNameValidator;

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	@ApiOperation(value = "All properties or filter by class name", notes = "Get all properties or filter by class name")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<PropertySummary>> listAllPropertyByClass(
			@PathVariable String cropname,
			@RequestParam(value = "class", defaultValue = "", required = false) String className)
			throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		if (Strings.isNullOrEmpty(className)) {
			List<PropertySummary> propertyList = this.ontologyPropertyService.getAllProperties();
			return new ResponseEntity<>(propertyList, HttpStatus.OK);
		}
		List<PropertySummary> propertyList = this.ontologyPropertyService.getAllPropertiesByClass(className);
		return new ResponseEntity<>(propertyList, HttpStatus.OK);

	}

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	@ApiOperation(value = "Get Property by id", notes = "Get Property using given Property id")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable String cropname,
															@PathVariable String id) throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		TermRequest request = new TermRequest(id, "property", CvId.PROPERTIES.getId());
		this.termValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyPropertyService.getProperty(Integer.valueOf(id)), HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@ApiOperation(value = "Add Property", notes = "Add a Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addProperty(@PathVariable String cropname,
													   @RequestBody PropertyRequestBase requestBase) throws MiddlewareException {
		ModelMapper modelMapper = OntologyMapper.propertyBaseToRequestMapper();
		PropertyRequest request = modelMapper.map(requestBase, PropertyRequest.class);
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.propertyRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyPropertyService.addProperty(request), HttpStatus.CREATED);
	}

	/**
	 * @param cropname
	 *            The name of the crop which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Property", notes = "Update Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateProperty(@PathVariable String cropname, @PathVariable String id,
										 @RequestBody PropertyRequestBase requestBase) throws MiddlewareException {

		ModelMapper modelMapper = OntologyMapper.getInstance();
		PropertyRequest request = modelMapper.map(requestBase, PropertyRequest.class);
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		request.setId(id);
		this.propertyRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyPropertyService.updateProperty(Integer.valueOf(id), request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Property", notes = "Delete Property using Given Id")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteProperty(@PathVariable String cropname, @PathVariable String id)
			throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.deletableValidator.validate(new TermRequest(id, "Property", CvId.PROPERTIES.getId()), bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyPropertyService.deleteProperty(Integer.valueOf(id));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
