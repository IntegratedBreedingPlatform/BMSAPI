package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.exception.ApiRequestValidationException;
import org.generationcp.bms.ontology.dto.TermRequest;
import org.generationcp.bms.ontology.dto.VariableSummary;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.validator.RequestIdValidator;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.HashMap;

/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Variable Service")
@Controller
@RequestMapping("/ontology")
public class OntologyVariableResource {
	
	@Autowired
	private OntologyModelService ontologyModelService;

    @Autowired
    private RequestIdValidator requestIdValidator;

    @Autowired
    private TermValidator termValidator;

	@ApiOperation(value = "All variables", notes = "Gets all variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableSummary>> listAllVariables(@PathVariable String  cropname) throws MiddlewareQueryException {
        return new ResponseEntity<>(ontologyModelService.getAllVariables(), HttpStatus.OK);
	}

    @ApiOperation(value = "Get Variable", notes = "Get Variable By Id")
    @RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getVariableById(@PathVariable String  cropname, @PathVariable String id) throws MiddlewareQueryException, MiddlewareException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
        requestIdValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        TermRequest request = new TermRequest(Integer.valueOf(id), CvId.VARIABLES.getId());
        termValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.getVariableById(Integer.valueOf(id)), HttpStatus.OK);
    }
}
