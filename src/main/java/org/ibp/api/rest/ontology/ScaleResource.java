
package org.ibp.api.rest.ontology;

import java.util.List;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.ContextHolder;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.java.ontology.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Scale Service")
@Controller
@RequestMapping("/ontology")
public class ScaleResource {

	@Autowired
	private ScaleService scaleService;

	@Autowired
	private ContextUtil contextUtil;

	@ApiOperation(value = "All Scales", notes = "Get all scales")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScaleDetails>> listAllScale(@PathVariable String cropname) {
		return new ResponseEntity<>(this.scaleService.getAllScales(), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Scale", notes = "Get Scale By Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ScaleDetails> getScaleById(@PathVariable String cropname, @PathVariable String id) {
		return new ResponseEntity<>(this.scaleService.getScaleById(id), HttpStatus.OK);
	}

	@ApiOperation(value = "Add Scale", notes = "Add new scale using detail")
	@RequestMapping(value = "/{cropname}/scales", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addScale(@PathVariable String cropname, @RequestBody ScaleDetails scaleSummary) {

		return new ResponseEntity<>(this.scaleService.addScale(scaleSummary), HttpStatus.CREATED);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Scale", notes = "Update existing scale using detail")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateScale(@PathVariable String cropname, @PathVariable String id, @RequestBody ScaleDetails scaleSummary) {

		// Set the program in the ContextHolder for this request.
		// This data is required in deleting Scales related variables from cache
		// when updating the scale variable.
		ContextHolder.setCurrentProgram(contextUtil.getCurrentProgramUUID());

		this.scaleService.updateScale(id, scaleSummary);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Scale", notes = "Delete Scale using Given Id")
	@RequestMapping(value = "/{cropname}/scales/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteScale(@PathVariable String cropname, @PathVariable String id) {

		this.scaleService.deleteScale(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
