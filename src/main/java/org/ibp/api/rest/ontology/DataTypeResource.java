
package org.ibp.api.rest.ontology;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Api(value = "Ontology Data Type Service")
@Controller
@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
@RequestMapping("/crops")
public class DataTypeResource {

	@Autowired
	private ModelService modelService;

	@ApiOperation(value = "All Data Types", notes = "Get all Data Types")
	@RequestMapping(value = "/{crop}/data-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<DataType>> listAllDataTypes(@PathVariable final String crop, @RequestParam final String programUUID) {
		return new ResponseEntity<>(this.modelService.getAllDataTypes(), HttpStatus.OK);
	}
}
