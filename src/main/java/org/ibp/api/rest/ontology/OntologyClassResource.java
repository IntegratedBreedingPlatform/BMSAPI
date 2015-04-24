package org.ibp.api.rest.ontology;

import java.util.HashMap;
import java.util.List;

import org.ibp.api.java.ontology.OntologyModelService;
import org.ibp.api.rest.AbstractResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Class Service")
@Controller
@RequestMapping("/ontology")
public class OntologyClassResource extends AbstractResource {

	@Autowired
	private OntologyModelService ontologyModelService;

	/**
	 * @param cropname
	 * 			The crop for which this rest call is being made
	 */
	@ApiOperation(value = "All Classes", notes = "Get all Classes")
	@RequestMapping(value = "/{cropname}/classes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<String>> listAllClasses(@PathVariable String cropname) {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Class");
		super.validateCropName(cropname, bindingResult);
		return new ResponseEntity<>(this.ontologyModelService.getAllClasses(), HttpStatus.OK);
	}
}
