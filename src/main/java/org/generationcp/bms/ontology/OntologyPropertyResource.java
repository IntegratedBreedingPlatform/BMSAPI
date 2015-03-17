package org.generationcp.bms.ontology;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.GenericResponse;
import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.bms.ontology.dto.PropertyResponse;
import org.generationcp.bms.ontology.dto.PropertySummary;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")
@SuppressWarnings("unused") // Added because it shows the cropname not used warning that is used in URL
public class OntologyPropertyResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyPropertyResource.class);

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
    public ResponseEntity<GenericResponse> addProperty(@PathVariable String  cropname, @RequestBody PropertyRequest request) throws MiddlewareQueryException, MiddlewareException {
        if(!request.isValid()){
            LOGGER.error("Not Enough Data to Add New Property");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        GenericResponse response = ontologyModelService.addProperty(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //TODO: 403 response for user without permission, Check if method is deletable or not
    @ApiOperation(value = "Delete Property", notes = "Delete Property using Given Id")
    @RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteProperty(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException, MiddlewareException {
        if (!ontologyModelService.deleteProperty(id)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    //TODO: 403 response for user without permission, Check if fields are editable or not
    @ApiOperation(value = "Update Property", notes = "Update Property using Given Data")
    @RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity updateProperty(@PathVariable String  cropname, @PathVariable Integer id, @RequestBody PropertyRequest request) throws MiddlewareQueryException, MiddlewareException {
        if(!request.isValid()) {
            LOGGER.error("Not Enough Data to Update Existing Property");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        ontologyModelService.updateProperty(id, request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
