package org.ibp.api.rest.ontology;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.IdName;
import org.ibp.api.java.ontology.OntologyModelService;
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

@Api(value = "Ontology Data Type Service")
@Controller
@RequestMapping("/ontology")
public class OntologyDataTypeResource {

	@Autowired
	private OntologyModelService ontologyModelService;

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	@ApiOperation(value = "All Data Types", notes = "Get all Data Types")
	@RequestMapping(value = "/{cropname}/datatypes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<IdName>> listAllDataTypes(@PathVariable String cropname)
			throws MiddlewareQueryException {
		return new ResponseEntity<>(this.ontologyModelService.getAllDataTypes(), HttpStatus.OK);
	}
}
