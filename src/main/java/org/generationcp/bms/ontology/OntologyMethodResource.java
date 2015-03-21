package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.exception.ApiRequestValidationException;
import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.ontology.dto.MethodResponse;
import org.generationcp.bms.ontology.dto.MethodSummary;
import org.generationcp.bms.ontology.dto.TermRequest;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.validator.MethodRequestValidator;
import org.generationcp.bms.ontology.validator.RequestIdValidator;
import org.generationcp.bms.ontology.validator.TermDeletableValidator;
import org.generationcp.middleware.domain.oms.CvId;
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
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;


@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
@SuppressWarnings("unused") // Added because it shows the cropname not used warning that is used in URL
public class OntologyMethodResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyMethodResource.class);

    @Autowired private RequestIdValidator requestIdValidator;

    @Autowired private MethodRequestValidator methodRequestValidator;

    @Autowired private TermDeletableValidator termDeletableValidator;

    @Autowired private OntologyModelService ontologyModelService;

    @ApiOperation(value = "All Methods", notes = "Get all methods")
    @RequestMapping(value = "/{cropname}/methods", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<MethodSummary>> listAllMethods(@PathVariable String  cropname) throws MiddlewareQueryException {
        List<MethodSummary> methodList = ontologyModelService.getAllMethods();
        return new ResponseEntity<>(methodList, HttpStatus.OK);
    }

    @ApiOperation(value = "Get method by id", notes = "Get method using given method id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getMethodById(@PathVariable String cropname, @PathVariable String id) throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        requestIdValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        MethodResponse method = ontologyModelService.getMethod(Integer.valueOf(id));
        if(method == null) {
            LOGGER.error("No Valid Method Found using Id " + id);
            return new ResponseEntity<>(BAD_REQUEST);
        }
		return new ResponseEntity<>(method, HttpStatus.OK);
	}

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Add Method", notes = "Add a Method using Given Data")
    @RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> addMethod(@PathVariable String  cropname,@RequestBody MethodRequest request, BindingResult bindingResult) throws MiddlewareQueryException {
        methodRequestValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        return new ResponseEntity<>(ontologyModelService.addMethod(request), CREATED);
    }

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Update Method", notes = "Update Method using Given Data")
    @RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<?> updateMethod(@PathVariable String  cropname,@PathVariable Integer id, @RequestBody MethodRequest request, BindingResult bindingResult) throws MiddlewareQueryException, MiddlewareException {
        request.setId(id);
        methodRequestValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        ontologyModelService.updateMethod(request.getId(), request);
        return new ResponseEntity(NO_CONTENT);
    }

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Delete Method", notes = "Delete Method using Given Id")
    @RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteMethod(@PathVariable String  cropname,@PathVariable Integer id) throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        termDeletableValidator.validate(new TermRequest(id, CvId.METHODS.getId()), bindingResult);
        if(bindingResult.hasErrors()){
            throw new ApiRequestValidationException(bindingResult.getAllErrors());
        }
        ontologyModelService.deleteMethod(id);
        return new ResponseEntity(NO_CONTENT);
    }
}
