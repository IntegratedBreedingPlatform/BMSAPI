package org.generationcp.bms.ontology;

import org.generationcp.bms.ontology.dto.outgoing.PropertySummary;
import org.generationcp.bms.ontology.dto.outgoing.PropertyResponse;
import org.generationcp.bms.ontology.services.IOntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")
public class OntologyPropertyResource {

    @Autowired
    private IOntologyModelService ontologyModelService;
	
	@ApiOperation(value = "All properties", notes = "Get all properties")
	@RequestMapping(value = "/{cropname}/properties/list", method = RequestMethod.GET)
	@ResponseBody
	public List<PropertySummary> listAllProperty(@PathVariable String  cropname) throws MiddlewareQueryException {
        return ontologyModelService.getAllProperties();
	}

    // TODO : editableFields and deletable need to be determined
    @ApiOperation(value = "Get Property by id", notes = "Get Property using given Property id")
    @RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException {
        PropertyResponse propertyResponse = ontologyModelService.getProperty(id);
        if(propertyResponse == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }
}
