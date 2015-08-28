package org.ibp.api.rest.ontology;

import java.util.List;

import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.ontology.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Services")
@RestController
@RequestMapping("/ontology")
public class OntologyResource {

	@Autowired
	private OntologyService ontologyService;
	/**
	 * @param cropname
	 * @return
	 */
	@ApiOperation(value = "Gets Trait Groups", notes = "Returns groups in which traits are classified.")
	@RequestMapping(value = "/{cropname}/traitgroups/", method = RequestMethod.GET)
	public ResponseEntity<List<Trait>> getTraitGroups(@PathVariable("cropname")String cropname){
		
		List<Trait> traits =  ontologyService.getTraitGroups();
		return new ResponseEntity<>(traits, HttpStatus.OK);
	}

	/**
	 * @param cropname
	 * @return
	 */
	@ApiOperation(value = "Get Traits", notes = "Retrieves traits from a particular trait group.")
	@RequestMapping(value = "/{cropname}/traits/{groupId}", method = RequestMethod.GET)
	public ResponseEntity<List<Trait>> getTraits(@PathVariable("cropname")String cropname, 
				@PathVariable("groupId")int groupId){
		
		List<Trait> traits =  ontologyService.getTraitsByGroup(groupId);
		return new ResponseEntity<>(traits, HttpStatus.OK);
	}

}
