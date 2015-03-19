package org.generationcp.bms.ontology;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.GenericResponse;
import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.bms.ontology.dto.PropertyResponse;
import org.generationcp.bms.ontology.dto.PropertySummary;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.validator.IntegerValidator;
import org.generationcp.bms.ontology.validator.PropertyDeletableValidator;
import org.generationcp.bms.ontology.validator.PropertyEditableValidator;
import org.generationcp.bms.ontology.validator.PropertyNullAndUniqueValidator;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")
@SuppressWarnings("unused") // Added because it shows the cropname not used warning that is used in URL
public class OntologyPropertyResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyPropertyResource.class);

    @Autowired
    private IntegerValidator integerValidator;
    @Autowired
    private PropertyNullAndUniqueValidator nullAndUniqueValidator;
    @Autowired
    private PropertyEditableValidator editableValidator;
    @Autowired
    private PropertyDeletableValidator deletableValidator;
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
    @RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getPropertyById(@PathVariable String  cropname, @PathVariable String id) throws MiddlewareQueryException, MiddlewareException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
        integerValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(bindingResult), BAD_REQUEST);
        }
        PropertyResponse propertyResponse = ontologyModelService.getProperty(Integer.valueOf(id));
        if(propertyResponse == null){
            LOGGER.error("No Valid Property Found using Id " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Add Property", notes = "Add a Property using Given Data")
    @RequestMapping(value = "/{cropname}/properties", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> addProperty(@PathVariable String  cropname, @RequestBody PropertyRequest request, BindingResult result) throws MiddlewareQueryException, MiddlewareException {
        nullAndUniqueValidator.validate(request, result);
        if(result.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(result), BAD_REQUEST);
        }
        GenericResponse response = ontologyModelService.addProperty(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //TODO: 403 response for user without permission, Check if method is deletable or not
    @ApiOperation(value = "Delete Property", notes = "Delete Property using Given Id")
    @RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteProperty(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException, MiddlewareException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");
        deletableValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(bindingResult), BAD_REQUEST);
        }
        ontologyModelService.deleteProperty(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    //TODO: 403 response for user without permission, Check if fields are editable or not
    @ApiOperation(value = "Update Property", notes = "Update Property using Given Data")
    @RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity updateProperty(@PathVariable String  cropname, @PathVariable Integer id, @RequestBody PropertyRequest request, BindingResult result) throws MiddlewareQueryException, MiddlewareException {
        request.setId(id);
        editableValidator.validate(request, result);
        if(result.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(result), BAD_REQUEST);
        }
        ontologyModelService.updateProperty(id, request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
