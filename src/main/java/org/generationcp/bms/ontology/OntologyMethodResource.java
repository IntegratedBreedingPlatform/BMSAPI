package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.incoming.AddMethodRequest;
import org.generationcp.bms.ontology.dto.outgoing.GenericAddResponse;
import org.generationcp.bms.ontology.dto.outgoing.MethodSummary;
import org.generationcp.bms.ontology.dto.outgoing.MethodResponse;
import org.generationcp.bms.ontology.services.IOntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
public class OntologyMethodResource {
	
    @Autowired
    private IOntologyModelService ontologyModelService;

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
        if(method == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
		return new ResponseEntity<>(method, HttpStatus.OK);
	}

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Add Method", notes = "Add a Method using Given Data")
    @RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GenericAddResponse> addMethod(@PathVariable String  cropname, @RequestBody AddMethodRequest request) throws MiddlewareQueryException {
        if(!request.validate()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        GenericAddResponse response = ontologyModelService.addMethod(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
