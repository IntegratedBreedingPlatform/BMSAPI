package org.generationcp.bms.resource;

import java.util.HashSet;
import java.util.Set;

import org.generationcp.bms.domain.StandardVariableBasicInfo;
import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.bms.web.UrlComposer;
import org.generationcp.middleware.domain.dms.StandardVariable;
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
	private UrlComposer urlComposer;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "redirect:/api-docs/default/ontology-resource";
	}
	
	@RequestMapping(value = "/var/list", method = RequestMethod.GET)
	@ResponseBody
	public Set<StandardVariableBasicInfo> listAllSummaries() throws MiddlewareQueryException {
		Set<StandardVariableBasicInfo> variableSummaries = new HashSet<StandardVariableBasicInfo>();
		// FIXME : Need a Middleware method to load all summaries - does not exist yet.
		// Just using existing method which loads deatils in loop - bad but okay for prototyping!

		Set<StandardVariable> allVariables = ontologyDataManager.getAllStandardVariables();
		for (StandardVariable var : allVariables) {
			StandardVariableBasicInfo varSummary = new StandardVariableBasicInfo(var.getId(), var.getProperty().getName(), var.getMethod().getName(), var.getScale().getName());

			if (var.getDataType() != null) {
				varSummary.setDataType(var.getDataType().getName());
			}

			if (var.getStoredIn() != null) {
				varSummary.setRole(var.getStoredIn().getName());
			}

			if (var.getIsA() != null) {
				varSummary.setTraitClass(var.getIsA().getName());
			}

			varSummary.setDetailsUrl(urlComposer.getVariableDetailsUrl(var.getId()));
			variableSummaries.add(varSummary);
		}
		return variableSummaries;
	}
	
	@RequestMapping(value = "/var/{id}", method = RequestMethod.GET)
	@ResponseBody
	public StandardVariable getDetailsById(@PathVariable Integer id) throws MiddlewareQueryException {
		StandardVariable svDetails = ontologyDataManager.getStandardVariable(id);
		if(svDetails == null) {
			throw new NotFoundException();
		}
		return svDetails;		
	}
}
