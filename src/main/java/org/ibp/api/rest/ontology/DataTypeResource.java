
package org.ibp.api.rest.ontology;

import java.util.List;

import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Data Type Service")
@Controller
@RequestMapping("/ontology")
public class DataTypeResource {

	@Autowired
	private ModelService modelService;

	@ApiOperation(value = "All Data Types", notes = "Get all Data Types")
	@RequestMapping(value = "/datatypes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<DataType>> listAllDataTypes() {
		return new ResponseEntity<>(this.modelService.getAllDataTypes(), HttpStatus.OK);
	}
}
