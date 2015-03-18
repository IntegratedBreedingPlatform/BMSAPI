package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.ScaleSummary;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


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

	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScaleSummary>> listAllScale(@PathVariable String  cropname) throws MiddlewareQueryException {
		return new ResponseEntity<>(ontologyModelService.getAllScales(), HttpStatus.OK);
	}
}
