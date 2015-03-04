package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.outgoing.DataTypeSummary;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(value = "Ontology Data Type Service")
@Controller
@RequestMapping("/ontology")
public class OntologyDataTypeResource {
	
	@Autowired
	private OntologyModelService ontologyModelService;

    @ApiOperation(value = "All Data Types", notes = "Get all Data Types")
    @RequestMapping(value = "/{cropname}/datatype/list", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<DataTypeSummary>> listAllDataTypes(@PathVariable String cropname) throws MiddlewareQueryException {
        List<DataTypeSummary> dataTypes = ontologyModelService.getAllDataTypes();
        return new ResponseEntity<>(dataTypes, HttpStatus.OK);
    }
}
