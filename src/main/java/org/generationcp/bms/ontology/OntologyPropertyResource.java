package org.generationcp.bms.ontology;

import org.generationcp.bms.ontology.util.Validator;
import org.generationcp.bms.ontology.dto.outgoing.PropertySummary;
import org.generationcp.bms.ontology.dto.outgoing.PropertyResponse;
import org.generationcp.bms.ontology.dto.incoming.AddPropertyRequest;
import org.generationcp.bms.ontology.dto.outgoing.GenericAddResponse;
import org.generationcp.bms.ontology.services.IOntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Arrays;


@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")
public class OntologyPropertyResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyPropertyResource.class);

    @Autowired
    private IOntologyModelService ontologyModelService;

	@ApiOperation(value = "All properties", notes = "Get all properties")
	@RequestMapping(value = "/{cropname}/properties/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<PropertySummary>> listAllProperty(@PathVariable String  cropname) throws MiddlewareQueryException {
        return new ResponseEntity<>(ontologyModelService.getAllProperties(), HttpStatus.OK);
	}

    @ApiOperation(value = "All properties by class name", notes = "Get all properties by class name")
    @RequestMapping(value = "/{cropname}/properties/class/{propertyClass}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<PropertySummary>> listAllPropertyByClass(@PathVariable String  cropname, @PathVariable String propertyClass) throws MiddlewareQueryException {
        if(Validator.validateIsEmpty(propertyClass)){
            LOGGER.error("Empty Request");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(ontologyModelService.getAllPropertiesByClass(propertyClass), HttpStatus.OK);
    }

    @ApiOperation(value = "All properties by search filter", notes = "Get all properties by search filter")
    @RequestMapping(value = "/{cropname}/properties/filter/{filter}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<PropertySummary>> listAllPropertyByFilter(@PathVariable String  cropname, @PathVariable String filter) throws MiddlewareQueryException {
        if(Validator.validateIsEmpty(filter)){
            LOGGER.error("Empty Request");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(ontologyModelService.getAllPropertiesByFilter(filter), HttpStatus.OK);
    }

    @ApiOperation(value = "All properties by class names", notes = "Get all properties by class names")
    @RequestMapping(value = "/{cropname}/properties/classes/{classes}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<PropertySummary>> listAllPropertyByClasses(@PathVariable String  cropname, @PathVariable String classes) throws MiddlewareQueryException {
        List<String> classList = Arrays.asList(classes.split(","));
        if(Validator.validateList(classList)){
            LOGGER.error("No Classes Specified to Get Properties");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(ontologyModelService.getAllPropertiesByClasses(classList), HttpStatus.OK);
    }

    // TODO : editableFields and deletable need to be determined
    @ApiOperation(value = "Get Property by id", notes = "Get Property using given Property id")
    @RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException {
        PropertyResponse propertyResponse = ontologyModelService.getProperty(id);
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
    public ResponseEntity<GenericAddResponse> addProperty(@PathVariable String  cropname, @RequestBody AddPropertyRequest request) throws MiddlewareQueryException {
        if(!request.validate()){
            LOGGER.error("Not Enough Data to Add New Property");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        GenericAddResponse response = ontologyModelService.addProperty(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
