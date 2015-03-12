package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;

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
	private OntologyService ontologyService;

	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.GET)
	@ResponseBody
	public List<Scale> listAllScale(@PathVariable String  cropname) throws MiddlewareQueryException {
		return ontologyService.getAllScales();
	}

	@ApiOperation(value = "Get Scale by Id", notes = "Get Scale using Scale Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Scale getScaleById(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException {
		return ontologyService.getScale(id);
	}
}
