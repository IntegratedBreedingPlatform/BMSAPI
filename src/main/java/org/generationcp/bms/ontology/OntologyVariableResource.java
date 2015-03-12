package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Variable Service")
@Controller
@RequestMapping("/ontology")
@SuppressWarnings("unused") // Added because it shows the cropname not used warning that is used in URL
public class OntologyVariableResource {
	
	@Autowired
	private OntologyService ontologyService;

	@ApiOperation(value = "All variables", notes = "Gets all standard variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	public Set<StandardVariable> listAllStandardVariables(@PathVariable String  cropname) throws MiddlewareQueryException {
        return ontologyService.getAllStandardVariables();
	}

	@ApiOperation(value = "All variables by property id", notes = "Get all standard variables using given property id")
	@RequestMapping(value = "/{cropname}/variables?property={id}", method = RequestMethod.GET)
	@ResponseBody
	public List<StandardVariable> listAllStandardVariablesByPropertyId(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException {
        return ontologyService.getStandardVariablesByProperty(id);
	}

}
