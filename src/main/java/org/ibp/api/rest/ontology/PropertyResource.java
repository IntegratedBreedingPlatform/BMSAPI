
package org.ibp.api.rest.ontology;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.java.ontology.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")

public class PropertyResource {

	@Autowired
	private PropertyService propertyService;

	@ApiOperation(value = "All properties or filter by class name", notes = "Get all properties or filter by class name")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<PropertySummary>> listAllPropertyByClass(@PathVariable String cropname, @RequestParam(value = "class", defaultValue = "", required = false) String className) {
		if (Strings.isNullOrEmpty(className)) {
			List<PropertySummary> propertyList = this.propertyService.getAllProperties();
			return new ResponseEntity<>(propertyList, HttpStatus.OK);
		}
		List<PropertySummary> propertyList = this.propertyService.getAllPropertiesByClass(className);
		return new ResponseEntity<>(propertyList, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Property by id", notes = "Get Property using given Property id")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<PropertyDetails> getPropertyById(@PathVariable String cropname, @PathVariable String id) {
		return new ResponseEntity<>(this.propertyService.getProperty(id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Property", notes = "Add a Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addProperty(@PathVariable String cropname, @RequestBody PropertySummary property) {
		return new ResponseEntity<>(this.propertyService.addProperty(property), HttpStatus.CREATED);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Property", notes = "Update Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateProperty(@PathVariable String cropname, @PathVariable String id, @RequestBody PropertySummary property) {

		this.propertyService.updateProperty(id, property);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Property", notes = "Delete Property using Given Id")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteProperty(@PathVariable String cropname, @PathVariable String id) {
		this.propertyService.deleteProperty(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
