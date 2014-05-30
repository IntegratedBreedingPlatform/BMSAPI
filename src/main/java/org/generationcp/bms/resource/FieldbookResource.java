package org.generationcp.bms.resource;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.bms.exception.NotFoundException;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fieldbook")
public class FieldbookResource {
	
	@Autowired
	private FieldbookService fieldbookService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(HttpServletRequest request) throws MiddlewareQueryException {
		return "Please provide the id of the nursery or trial you want to GET to. e.g. http://host:port/fieldbook/10010 where 10010 is the nursery or study id.";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Workbook get(@PathVariable Integer id) throws MiddlewareQueryException {
		
		Workbook workbook = fieldbookService.getNurseryDataSet(id);
		if(workbook == null) {
			throw new NotFoundException(); 
		}
		return workbook;
	}

}
