package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Author: Sunny
 * Created Date: 2 Feb 2015
 */

@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology/properties")
public class OntologyPropertyResource {
	
	@Autowired
	private OntologyService ontologyService;
	
	@ApiOperation(value = "All properties", notes = "Get all properties")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public List<Property> listAllProperty() throws MiddlewareQueryException {
		List<Property> propertyList = ontologyService.getAllProperties();
		return propertyList;
	}

    @ApiOperation(value = "Property By Id", notes = "Get Property By Id")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Property getPropertyById(@PathVariable Integer id) throws MiddlewareQueryException {
        Property property = ontologyService.getProperty(id);
        return property;
    }
}
