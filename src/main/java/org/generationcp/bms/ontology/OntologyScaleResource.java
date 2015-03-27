package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.generationcp.bms.exception.ApiRequestValidationException;
import org.generationcp.bms.ontology.dto.*;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.validator.RequestIdValidator;
import org.generationcp.bms.ontology.validator.ScaleRequestValidator;
import org.generationcp.bms.ontology.validator.TermDeletableValidator;
import org.generationcp.bms.ontology.validator.TermValidator;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Scale Service")
@Controller
@RequestMapping("/ontology")
public class OntologyScaleResource {
	
	@Autowired
	private OntologyModelService ontologyModelService;

    @Autowired
    private TermValidator termValidator;

    @Autowired
    private RequestIdValidator requestIdValidator;

    @Autowired
    private ScaleRequestValidator scaleRequestValidator;

    @Autowired
    private TermDeletableValidator termDeletableValidator;

	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScaleSummary>> listAllScale(@PathVariable String  cropname) throws MiddlewareQueryException {
		return new ResponseEntity<>(ontologyModelService.getAllScales(), HttpStatus.OK);
	}

    @ApiOperation(value = "Get Scale", notes = "Get Scale By Id")
    @RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ScaleResponse> getScaleById(@PathVariable String  cropname, @PathVariable String id) throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
        requestIdValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        TermRequest request = new TermRequest(Integer.valueOf(id), CvId.SCALES.getId());
        termValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.getScaleById(Integer.valueOf(id)), HttpStatus.OK);
    }

    @ApiOperation(value = "Add Scale", notes = "Add new scale using detail")
    @RequestMapping(value = "/{cropname}/scales", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GenericResponse> addScale(@PathVariable String  cropname, @RequestBody ScaleRequest request, BindingResult bindingResult) throws MiddlewareQueryException, MiddlewareException {
        scaleRequestValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.addScale(request), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update Scale", notes = "Update existing scale using detail")
    @RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity updateScale(@PathVariable String  cropname, @PathVariable Integer id, @RequestBody ScaleRequest request, BindingResult result) throws MiddlewareQueryException, MiddlewareException, ApiRequestValidationException {
        request.setId(id);
        scaleRequestValidator.validate(request, result);
        if(result.hasErrors()){
            throw new ApiRequestValidationException(result.getAllErrors());
        }
        ontologyModelService.updateScale(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

	@ApiOperation(value = "Delete Scale", notes = "Delete Scale using Given Id")
    @RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteScale(@PathVariable String  cropname,@PathVariable Integer id) throws MiddlewareQueryException, MiddlewareException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
        termDeletableValidator.validate(new TermRequest(id, CvId.SCALES.getId()), bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        ontologyModelService.deleteScale(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
