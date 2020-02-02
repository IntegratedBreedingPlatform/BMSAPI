
package org.ibp.api.rest.ontology;

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.java.ontology.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
@RequestMapping("/crops")
public class PropertyResource {

	@Autowired
	private PropertyService propertyService;

	@ApiOperation(value = "All properties or filter by class name", notes = "Get all properties or filter by class name")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<PropertyDetails>> listAllPropertyByClass(@PathVariable final String cropname,
		@RequestParam final String programUUID, @RequestParam(value = "class",
		defaultValue = "", required = false) final String className) {
		if (Strings.isNullOrEmpty(className)) {
			final List<PropertyDetails> propertyList = this.propertyService.getAllProperties();
			return new ResponseEntity<>(propertyList, HttpStatus.OK);
		}
		final List<PropertyDetails> propertyList = this.propertyService.getAllPropertiesByClass(className);
		return new ResponseEntity<>(propertyList, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Property by id", notes = "Get Property using given Property id")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<PropertyDetails> getPropertyById(@PathVariable final String cropname, @PathVariable final String id,
		@RequestParam final String programUUID) {
		return new ResponseEntity<>(this.propertyService.getProperty(id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Property", notes = "Add a Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addProperty(@PathVariable final String cropname, @RequestParam final String programUUID,
		@RequestBody final PropertyDetails property) {
		return new ResponseEntity<>(this.propertyService.addProperty(property), HttpStatus.CREATED);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Property", notes = "Update Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateProperty(@PathVariable final String cropname, @PathVariable final String id,
		@RequestParam final String programUUID, @RequestBody final PropertyDetails property) {

		this.propertyService.updateProperty(id, property);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Property", notes = "Delete Property using Given Id")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteProperty(@PathVariable final String cropname, @PathVariable final String id,
		@RequestParam final String programUUID) {
		this.propertyService.deleteProperty(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
