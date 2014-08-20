package org.generationcp.bms.resource;

import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/fieldbook")
public class FieldbookResource {
	
	@Autowired
	private FieldbookService fieldbookService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "redirect:/api-docs/default/fieldbook-resource";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Workbook get(@PathVariable Integer id) throws MiddlewareQueryException {
		
		Workbook workbook = fieldbookService.getNurseryDataSet(id);
		if(workbook == null) {
			throw new NotFoundException(); 
		}
		return workbook;
	}

}
