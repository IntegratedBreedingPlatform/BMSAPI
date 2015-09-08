
package org.ibp.api.rest.ontology;

import java.util.List;

import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Class Service")
@Controller
@RequestMapping("/ontology")
public class ClassResource {

	@Autowired
	private ModelService modelService;

	@ApiOperation(value = "All Classes", notes = "Get all Classes")
	@RequestMapping(value = "/{cropname}/classes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<String>> listAllClasses(@PathVariable String cropname) {
		return new ResponseEntity<>(this.modelService.getAllClasses(), HttpStatus.OK);
	}
}
