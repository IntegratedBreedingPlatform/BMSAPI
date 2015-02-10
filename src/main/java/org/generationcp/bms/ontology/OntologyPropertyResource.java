package org.generationcp.bms.ontology;

import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.service.api.OntologyService;
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

    @ApiOperation(value = "Property By Filter", notes = "Get Property By Filter Text")
    @RequestMapping(value = "/filter/{text}", method = RequestMethod.GET)
    @ResponseBody
    public Property getPropertyByFilter(@PathVariable String text) throws MiddlewareQueryException {
        Property property = ontologyService.getProperty(text);
        return property;
    }

    @ApiOperation(value = "Properties With Trait Class", notes = "Get All Properties with Trait Class")
    @RequestMapping(value = "/class/list", method = RequestMethod.GET)
    @ResponseBody
    public List<Property> listPropertiesByClass() throws MiddlewareQueryException {
        List<Property> propertyList = ontologyService.getAllPropertiesWithTraitClass();
        return propertyList;
    }
}
