package org.generationcp.bms.ontology;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.exception.ApiRequestValidationException;
import org.generationcp.bms.ontology.dto.*;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.validator.PropertyRequestValidator;
import org.generationcp.bms.ontology.validator.RequestIdValidator;
import org.generationcp.bms.ontology.validator.TermDeletableValidator;
import org.generationcp.bms.ontology.validator.TermValidator;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
    private OntologyModelService ontologyModelService;

	@ApiOperation(value = "All properties or filter by class name", notes = "Get all properties or filter by class name")
    @RequestMapping(value = "/{cropname}/properties", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<PropertySummary>> listAllPropertyByClass(@PathVariable String  cropname, @RequestParam(value = "class", defaultValue = "", required = false) String className) throws MiddlewareQueryException {
        if(Strings.isNullOrEmpty(className)){
            List<PropertySummary> propertyList = ontologyModelService.getAllProperties();
            return new ResponseEntity<>(propertyList, HttpStatus.OK);
        }else {
            List<PropertySummary> propertyList = ontologyModelService.getAllPropertiesByClass(className);
            return new ResponseEntity<>(propertyList, HttpStatus.OK);
        }
    }

    @ApiOperation(value = "Get Property by id", notes = "Get Property using given Property id")
    @RequestMapping(value = "/{cropname}/properties/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable String  cropname, @PathVariable String id) throws MiddlewareQueryException, MiddlewareException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
        requestIdValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        TermRequest request = new TermRequest(Integer.valueOf(id), "property", CvId.PROPERTIES.getId());
        termValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.getProperty(Integer.valueOf(id)), HttpStatus.OK);
    }

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Add Property", notes = "Add a Property using Given Data")
    @RequestMapping(value = "/{cropname}/properties", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GenericResponse> addProperty(@PathVariable String  cropname, @RequestBody PropertyRequest request, BindingResult bindingResult) throws MiddlewareQueryException, MiddlewareException {
        propertyRequestValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.addProperty(request), HttpStatus.CREATED);
    }

    //TODO: 403 response for user without permission
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Property", notes = "Delete Property using Given Id")
    @RequestMapping(value = "/{cropname}/properties/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteProperty(@PathVariable String  cropname, @PathVariable String id) throws MiddlewareQueryException, MiddlewareException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        requestIdValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }

        deletableValidator.validate(new TermRequest(Integer.valueOf(id), "property", CvId.PROPERTIES.getId()), bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        ontologyModelService.deleteProperty(Integer.valueOf(id));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //TODO: 403 response for user without permission
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Property", notes = "Update Property using Given Data")
    @RequestMapping(value = "/{cropname}/properties/{id:.+}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity updateProperty(@PathVariable String  cropname, @PathVariable String id, @RequestBody PropertyRequest request, BindingResult bindingResult) throws MiddlewareQueryException, MiddlewareException {
        requestIdValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        request.setId(Integer.valueOf(id));
        propertyRequestValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        ontologyModelService.updateProperty(Integer.valueOf(id), request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
