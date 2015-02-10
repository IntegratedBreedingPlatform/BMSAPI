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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Set;

/**
 * Author: Sunny
 * Created Date: 2 Feb 2015
 */

@Api(value = "Ontology Variable Service")
@Controller
@RequestMapping("/ontology/variables")
public class OntologyVariableResource {
	
	@Autowired
	private OntologyService ontologyService;
	
	@ApiOperation(value = "All variables", notes = "Gets all standard variables.")
	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseBody
	public Set<StandardVariable> listAllStandardVariables() throws MiddlewareQueryException {
		Set<StandardVariable> allVariables = ontologyService.getAllStandardVariables();
		return allVariables;
	}

	@ApiOperation(value = "All variables by property id", notes = "Get all standard variables using given property id")
	@RequestMapping(value = "/property/{id}", method = RequestMethod.GET)
	@ResponseBody
	public List<StandardVariable> listAllStandardVariablesByPropertyId(@PathVariable Integer id) throws MiddlewareQueryException {
		List<StandardVariable> variables = ontologyService.getStandardVariablesByProperty(id);
		return variables;
	}

    @ApiOperation(value = "All variables by search term", notes = "Get all standard variables using search term")
    @RequestMapping(value = "/filter/{text}", method = RequestMethod.GET)
    @ResponseBody
    public List<StandardVariable> listAllStandardVariablesByFilter(@PathVariable String text) throws MiddlewareQueryException {
        List<StandardVariable> variables = ontologyService.getStandardVariables(text);
        return variables;
    }
}
