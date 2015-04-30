package org.ibp.api.rest.ontology;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.ScaleRequest;
import org.ibp.api.domain.ontology.ScaleRequestBase;
import org.ibp.api.domain.ontology.ScaleResponse;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.OntologyMapper;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.ScaleRequestValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.OntologyScaleService;
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

@Api(value = "Ontology Scale Service")
@Controller
@RequestMapping("/ontology")
public class OntologyScaleResource extends AbstractResource {

  	@Autowired
	private OntologyScaleService ontologyScaleService;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private RequestIdValidator requestIdValidator;

	@Autowired
	private ScaleRequestValidator scaleRequestValidator;

	@Autowired
	private TermDeletableValidator termDeletableValidator;

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable types.
	 */
	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScaleSummary>> listAllScale(@PathVariable String cropname) {
		return new ResponseEntity<>(this.ontologyScaleService.getAllScales(), HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable
	 *            types.
	 */
	@ApiOperation(value = "Get Scale", notes = "Get Scale By Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ScaleResponse> getScaleById(@PathVariable String cropname,
			@PathVariable String id)  {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		TermRequest request = new TermRequest(id, "scale", CvId.SCALES.getId());
		this.termValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyScaleService.getScaleById(Integer.valueOf(id)),
				HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable
	 *            types.
	 */
	@ApiOperation(value = "Add Scale", notes = "Add new scale using detail")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addScale(@PathVariable String cropname,
			@RequestBody ScaleRequestBase requestBase)  {

		ModelMapper modelMapper = OntologyMapper.getInstance();
		ScaleRequest request = modelMapper.map(requestBase, ScaleRequest.class);

	  	BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
		this.scaleRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyScaleService.addScale(request), HttpStatus.CREATED);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable
	 *            types.
	 */
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Scale", notes = "Update existing scale using detail")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateScale(@PathVariable String cropname, @PathVariable String id,
			@RequestBody ScaleRequestBase requestBase) throws ApiRequestValidationException {

		ModelMapper modelMapper = OntologyMapper.getInstance();
		ScaleRequest request = modelMapper.map(requestBase, ScaleRequest.class);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		request.setId(id);
		this.scaleRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyScaleService.updateScale(request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable
	 *            types.
	 */
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Scale", notes = "Delete Scale using Given Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteScale(@PathVariable String cropname, @PathVariable String id)  {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.termDeletableValidator.validate(new TermRequest(id, "Scale", CvId.SCALES.getId()), bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyScaleService.deleteScale(Integer.valueOf(id));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
