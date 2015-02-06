package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.oms.Method;
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
 * Created Date: 5 Feb 2015
 */

@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology/methods")
public class OntologyMethodResource {
	
	@Autowired
	private OntologyService ontologyService;
	
	@ApiOperation(value = "All Methods", notes = "Get all methods")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public List<Method> listAllMethods() throws MiddlewareQueryException {
		List<Method> methodList = ontologyService.getAllMethods();
		return methodList;
	}

	@ApiOperation(value = "Get method by id", notes = "Get method using given method id")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Method getMethodById(@PathVariable Integer id) throws MiddlewareQueryException {
		Method method = ontologyService.getMethod(id);
		return method;
	}

    @ApiOperation(value = "Get method by name", notes = "Get method given method name")
    @RequestMapping(value = "/name/{name}", method = RequestMethod.GET)
    @ResponseBody
    public Method getMethodByName(@PathVariable String name) throws MiddlewareQueryException {
        Method method = ontologyService.getMethod(name);
        return method;
    }
}
