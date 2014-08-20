package org.generationcp.bms.resource;

import java.util.List;
import java.util.Set;

import org.generationcp.bms.dao.MongoDao;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/ontology")
public class OntologyResource {
	
	@Autowired
	private OntologyDataManager ontologyDataManager;
	
	@Autowired
	private MongoDao mongoDao;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "redirect:/api-docs/default/ontology-resource";
	}
	
	@RequestMapping(value = "/var/{id}", method = RequestMethod.GET)
	@ResponseBody
	public StandardVariableSummary getSummaryById(@PathVariable Integer id) throws MiddlewareQueryException {
		StandardVariableSummary svSummary = ontologyDataManager.getStandardVariableSummary(id);
		if(svSummary == null) {
			throw new NotFoundException();
		}
		return svSummary;		
	}
	
	@RequestMapping(value = "/var/{id}/details", method = RequestMethod.GET)
	@ResponseBody
	public StandardVariable getDetailsById(@PathVariable Integer id) throws MiddlewareQueryException {
		StandardVariable svDetails = ontologyDataManager.getStandardVariable(id);
		if(svDetails == null) {
			throw new NotFoundException();
		}
		return svDetails;		
	}
	
	@RequestMapping(value = "/var/all/details", method = RequestMethod.GET)
	@ResponseBody
	public Set<StandardVariable> getAllDetailed() throws MiddlewareQueryException {
		return ontologyDataManager.getAllStandardVariables();
	}
	
	@RequestMapping(value = "/var/all/mongo", method = RequestMethod.GET)
	@ResponseBody
	public List<StandardVariable> getAllMongo() throws MiddlewareQueryException {
		return mongoDao.getAllStandardVariables();
	}
}
