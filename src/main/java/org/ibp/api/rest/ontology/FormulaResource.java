package org.ibp.api.rest.ontology;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.ibp.api.java.ontology.FormulaService;
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

@Api(value = "Ontology Formula Services")
@Controller
@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_ONTOLOGIES')")
@RequestMapping("/crops")
public class FormulaResource {

	@Autowired
	private FormulaService formulaService;

	@ApiOperation(value = "Create Formula", notes = "Create a formula to calculate a Variable")
	@RequestMapping(value = "/{cropname}/formula", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<FormulaDto> createFormula(
		@PathVariable final String cropname,
		@RequestParam final String programUUID,
		@ApiParam("Formula object to create. Inputs will be extracted from the formula definition."
			+ " All other info about inputs will be discarded")
		@RequestBody final FormulaDto formulaDto) {
		return new ResponseEntity<>(this.formulaService.save(formulaDto), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Delete Formula", notes = "Mark formula as inactive")
	@RequestMapping(value = "/{cropname}/formula/{formulaId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> deleteFormula(
		@PathVariable final String cropname,
		@PathVariable final Integer formulaId,
		@RequestParam final String programUUID) {

		this.formulaService.delete(formulaId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Update Formula", notes = "Update a formula in a Variable")
	@RequestMapping(value = "/{cropname}/formula/{formulaId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<FormulaDto> updateFormula(
		@PathVariable final String cropname,
		@PathVariable final Integer formulaId,
		@RequestParam final String programUUID,
		@RequestBody final FormulaDto formulaDto) {

		return new ResponseEntity<>(this.formulaService.update(formulaDto),HttpStatus.OK);
	}
}
