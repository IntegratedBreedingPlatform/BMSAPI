package org.generationcp.bms.ontology;

import org.generationcp.bms.ontology.dto.outgoing.PropertySummary;
import org.generationcp.bms.ontology.services.IOntologyModelService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")
public class OntologyPropertyResource {

    @Autowired
    private IOntologyModelService ontologyModelService;
	
	@ApiOperation(value = "All properties", notes = "Get all properties")
	@RequestMapping(value = "/{cropname}/properties/list", method = RequestMethod.GET)
	@ResponseBody
	public List<PropertySummary> listAllProperty(@PathVariable String  cropname) throws MiddlewareQueryException {
        return ontologyModelService.getAllProperties();
	}

}
