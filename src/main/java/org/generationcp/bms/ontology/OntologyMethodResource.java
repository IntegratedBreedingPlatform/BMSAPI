package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.outgoing.MethodSummary;
import org.generationcp.bms.ontology.dto.outgoing.MethodResponse;
import org.generationcp.bms.ontology.dto.incoming.AddMethodRequest;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.dto.outgoing.GenericAddResponse;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
public class OntologyMethodResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyMethodResource.class);
	
    @Autowired
    private OntologyModelService ontologyModelService;

    @ApiOperation(value = "All Methods", notes = "Get all methods")
    @RequestMapping(value = "/{cropname}/methods/list", method = RequestMethod.GET)
    @ResponseBody
    public List<MethodSummary> listAllMethods(@PathVariable String  cropname) throws MiddlewareQueryException {
        return ontologyModelService.getAllMethods();
    }

    // TODO : editableFields and deletable need to be determined
    @ApiOperation(value = "Get method by id", notes = "Get method using given method id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<MethodResponse> getMethodById(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException {
		MethodResponse method = ontologyModelService.getMethod(id);
        if(method == null) {
            LOGGER.error("No Valid Method Found using Id " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
		return new ResponseEntity<>(method, HttpStatus.OK);
	}

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Add Method", notes = "Add a Method using Given Data")
    @RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GenericAddResponse> addMethod(@PathVariable String  cropname, @RequestBody AddMethodRequest request) throws MiddlewareQueryException {
        if(!request.validate()){
            LOGGER.error("Not Enough Data to Add New Method");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        GenericAddResponse response = ontologyModelService.addMethod(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //TODO: 403 response for user without permission, Check if fields are editable or not
    @ApiOperation(value = "Update Method", notes = "Update Method using Given Data")
    @RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity updateMethod(@PathVariable String  cropname, @PathVariable Integer id, @RequestBody AddMethodRequest request) throws MiddlewareQueryException, MiddlewareException {
        if(!request.validate()) {
            LOGGER.error("Not Enough Data to Update Existing Method");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        ontologyModelService.updateMethod(id, request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    //TODO: 403 response for user without permission, Check if method is deletable or not
    @ApiOperation(value = "Delete Method", notes = "Delete Method using Given Id")
    @RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteMethod(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException, MiddlewareException {
        ontologyModelService.deleteMethod(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
