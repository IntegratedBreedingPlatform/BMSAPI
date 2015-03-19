package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.ScaleRequest;
import org.generationcp.bms.ontology.dto.ScaleSummary;
import org.generationcp.bms.ontology.dto.TermRequest;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.validator.IntegerValidator;
import org.generationcp.bms.ontology.validator.ScaleRequestValidator;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Scale Service")
@Controller
@RequestMapping("/ontology")
@SuppressWarnings("unused") // Added because it shows the cropname not used warning that is used in URL
public class OntologyScaleResource {
	
	@Autowired
	private OntologyModelService ontologyModelService;

    @Autowired
    private TermValidator termValidator;

    @Autowired
    private IntegerValidator integerValidator;

    @Autowired
    private ScaleRequestValidator scaleRequestValidator;

	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScaleSummary>> listAllScale(@PathVariable String  cropname) throws MiddlewareQueryException {
		return new ResponseEntity<>(ontologyModelService.getAllScales(), HttpStatus.OK);
	}

    @ApiOperation(value = "Get Scale", notes = "Get Scale By Id")
    @RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getScaleById(@PathVariable String  cropname, @PathVariable String id) throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");
        integerValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(bindingResult), BAD_REQUEST);
        }
        TermRequest request = new TermRequest(Integer.valueOf(id), CvId.SCALES.getId());
        termValidator.validate(request, bindingResult);
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(bindingResult), BAD_REQUEST);
        }
        return new ResponseEntity<>(ontologyModelService.getScaleById(Integer.valueOf(id)), HttpStatus.OK);
    }

    @ApiOperation(value = "Add Scale", notes = "Add new scale using detail")
    @RequestMapping(value = "/{cropname}/scales", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> addScale(@PathVariable String  cropname, @RequestBody ScaleRequest request, BindingResult result) throws MiddlewareQueryException, MiddlewareException {
        scaleRequestValidator.validate(request, result);
        if(result.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(result), BAD_REQUEST);
        }
        return new ResponseEntity<>(ontologyModelService.addScale(request), HttpStatus.OK);
    }
}
