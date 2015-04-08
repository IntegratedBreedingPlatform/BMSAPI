package org.ibp.api.rest.ontology;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.*;
import org.ibp.api.java.ontology.OntologyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private ProgramValidator programValidator;

    @Autowired
    private VariableRequestValidator variableRequestValidator;

	@ApiOperation(value = "All variables", notes = "Gets all variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableSummary>> listAllVariables(@PathVariable String  cropname, @RequestParam(value = "property", required = false) String propertyId, @RequestParam(value = "favourite", required = false) Boolean favourite, @RequestParam(value = "programId") String programId) throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
        programValidator.validate(programId, bindingResult);

        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }

        Integer pId = null;

        if(!Strings.isNullOrEmpty(propertyId)){
            requestIdValidator.validate(propertyId, bindingResult);
            if(bindingResult.hasErrors()){
                throw new ApiRequestValidationException(bindingResult.getAllErrors());
            }
            pId = Integer.valueOf(propertyId);
        }
        return new ResponseEntity<>(ontologyModelService.getAllVariablesByFilter(Integer.valueOf(programId), pId, favourite), HttpStatus.OK);
	}

    @ApiOperation(value = "Get Variable", notes = "Get Variable By Id")
    @RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<VariableResponse> getVariableById(@PathVariable String  cropname,@RequestParam(value = "programId") String programId, @PathVariable String id) throws MiddlewareQueryException, MiddlewareException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
        programValidator.validate(programId, bindingResult);
        requestIdValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        TermRequest request = new TermRequest(Integer.valueOf(id), "variable", CvId.VARIABLES.getId());
        termValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.getVariableById(Integer.valueOf(programId), Integer.valueOf(id)), HttpStatus.OK);
    }

    @ApiOperation(value = "Add Variable", notes = "Add new variable using given data")
    @RequestMapping(value = "/{cropname}/variables", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GenericResponse> addVariable(@PathVariable String  cropname, @RequestBody VariableRequest request, BindingResult bindingResult) throws MiddlewareQueryException, MiddlewareException {
        variableRequestValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.addVariable(request), HttpStatus.CREATED);
    }

}
