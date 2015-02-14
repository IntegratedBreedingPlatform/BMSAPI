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
 * Author: Sunny
 * Created Date: 2 Feb 2015
 */

@Api(value = "Ontology Scale Service")
@Controller
@RequestMapping("/ontology")
public class OntologyScaleResource {
	
	@Autowired
	private OntologyService ontologyService;

	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales/list", method = RequestMethod.GET)
	@ResponseBody
	public List<Scale> listAllScale(@PathVariable String  cropname) throws MiddlewareQueryException {
		List<Scale> scaleList = ontologyService.getAllScales();
		return scaleList;
	}

	@ApiOperation(value = "Get Scale by Id", notes = "Get Scale using Scale Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Scale listAllStandardVariablesByScaleId(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException {
		Scale scale = ontologyService.getScale(id);
		return scale;
	}

    @ApiOperation(value = "Get Scale by Filter", notes = "Get Scale using Filter term")
    @RequestMapping(value = "/{cropname}/scales/filter/{text}", method = RequestMethod.GET)
    @ResponseBody
    public Scale listAllStandardVariablesByFilter(@PathVariable String  cropname, @PathVariable String text) throws MiddlewareQueryException {
        Scale scale = ontologyService.getScale(text);
        return scale;
    }
}
