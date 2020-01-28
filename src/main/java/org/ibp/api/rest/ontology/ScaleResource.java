package org.ibp.api.rest.ontology;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.java.ontology.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "Ontology Scale Service")
@RestController
@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
@RequestMapping("/crops")
public class ScaleResource {

	@Autowired
	private ScaleService scaleService;

	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScaleDetails>> listAllScale(@PathVariable final String cropname, @RequestParam final String programUUID) {
		return new ResponseEntity<>(this.scaleService.getAllScales(), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Scale", notes = "Get Scale By Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ScaleDetails> getScaleById(@PathVariable final String cropname, @PathVariable final String id,  @RequestParam final String programUUID) {
		return new ResponseEntity<>(this.scaleService.getScaleById(id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Scale", notes = "Add new scale using detail")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addScale(@PathVariable final String cropname,  @RequestParam final String programUUID, @RequestBody final ScaleDetails scaleSummary) {

		return new ResponseEntity<>(this.scaleService.addScale(scaleSummary), HttpStatus.CREATED);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Scale", notes = "Update existing scale using detail")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateScale(@PathVariable final String cropname, @PathVariable final String id,  @RequestParam final String programUUID,
			@RequestBody final ScaleDetails scaleSummary) {

		this.scaleService.updateScale(id, scaleSummary);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Scale", notes = "Delete Scale using Given Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteScale(@PathVariable final String cropname, @PathVariable final String id, @RequestParam final String programUUID) {

		this.scaleService.deleteScale(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
