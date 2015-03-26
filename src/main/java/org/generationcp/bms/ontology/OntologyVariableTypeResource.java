package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.IdName;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(value = "Ontology Variable Type Service")
@Controller
@RequestMapping("/ontology")
public class OntologyVariableTypeResource {
	
	@Autowired
	private OntologyModelService ontologyModelService;

    @ApiOperation(value = "All Classes", notes = "Get all Classes")
    @RequestMapping(value = "/{cropname}/variableTypes", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<IdName>> listAllVariableTypes(@PathVariable String cropname) throws MiddlewareQueryException {
        return new ResponseEntity<>(ontologyModelService.getAllVariableTypes(), HttpStatus.OK);
    }
}
