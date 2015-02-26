package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.outgoing.MethodDTO;
import org.generationcp.bms.ontology.services.IOntologyModelService;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
public class OntologyMethodResource {
	
	@Autowired
	private OntologyService ontologyService;

    @Autowired
    private IOntologyModelService ontologyModelService;

    @ApiOperation(value = "All Methods", notes = "Get all methods")
    @RequestMapping(value = "/{cropname}/methods/list", method = RequestMethod.GET)
    @ResponseBody
    public List<MethodDTO> listAllMethods(@PathVariable String  cropname) throws MiddlewareQueryException {
        return ontologyModelService.getAllMethods();
    }

	@ApiOperation(value = "Get method by id", notes = "Get method using given method id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Method getMethodById(@PathVariable String  cropname, @PathVariable Integer id) throws MiddlewareQueryException {
		Method method = ontologyService.getMethod(id);
		return method;
	}
}
