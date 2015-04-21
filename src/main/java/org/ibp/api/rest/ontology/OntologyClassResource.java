package org.ibp.api.rest.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidator;
import org.ibp.api.java.ontology.OntologyModelService;
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

import java.util.HashMap;
import java.util.List;

@Api(value = "Ontology Class Service")
@Controller
@RequestMapping("/ontology")
public class OntologyClassResource {

	@Autowired
	private OntologyModelService ontologyModelService;

	@Autowired
	private CropNameValidator cropNameValidator;

	/**
	 * @param cropname
	 * 			The crop for which this rest call is being made
	 */
	@ApiOperation(value = "All Classes", notes = "Get all Classes")
	@RequestMapping(value = "/{cropname}/classes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<String>> listAllClasses(@PathVariable String cropname) {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Class");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyModelService.getAllClasses(), HttpStatus.OK);
	}
}
