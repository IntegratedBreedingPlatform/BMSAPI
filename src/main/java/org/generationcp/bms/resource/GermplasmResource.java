package org.generationcp.bms.resource;

import java.util.List;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.GermplasmSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/germplasm")
public class GermplasmResource {

	private final SimpleDao simpleDao;
	
	@Autowired
	public GermplasmResource(SimpleDao dao) {
		this.simpleDao = dao;
	}
	
	@RequestMapping(value="/search", method = RequestMethod.GET)
	public List<GermplasmSearchResult> search(@RequestParam String q) {
		return simpleDao.searchGermplasm(q);
	}
	
}
