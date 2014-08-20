package org.generationcp.bms.resource;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.bms.dao.MongoDao;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ontology")
public class OntologyResource {
	
	@Autowired
	private OntologyDataManager ontologyDataManager;
	
	@Autowired
	private MongoDao mongoDao;
	
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	public String home(HttpServletRequest request) throws MiddlewareQueryException {
		return "Please provide the id of the standard variable you want to GET to. e.g. http://host:port/ontology/var/18020 where 18020 is the variable id.";
	}
	
	@RequestMapping(value = "/var/{id}", method = RequestMethod.GET)
	public StandardVariableSummary getSummaryById(@PathVariable Integer id) throws MiddlewareQueryException {
		StandardVariableSummary svSummary = ontologyDataManager.getStandardVariableSummary(id);
		if(svSummary == null) {
			throw new NotFoundException();
		}
		return svSummary;		
	}
	
	@RequestMapping(value = "/var/{id}/details", method = RequestMethod.GET)
	public StandardVariable getDetailsById(@PathVariable Integer id) throws MiddlewareQueryException {
		StandardVariable svDetails = ontologyDataManager.getStandardVariable(id);
		if(svDetails == null) {
			throw new NotFoundException();
		}
		return svDetails;		
	}
	
	@RequestMapping(value = "/var/all/details", method = RequestMethod.GET)
	public Set<StandardVariable> getAllDetailed() throws MiddlewareQueryException {
		return ontologyDataManager.getAllStandardVariables();
	}
	
	@RequestMapping(value = "/var/all/mongo", method = RequestMethod.GET)
	public List<StandardVariable> getAllMongo() throws MiddlewareQueryException {
		return mongoDao.getAllStandardVariables();
	}
}
