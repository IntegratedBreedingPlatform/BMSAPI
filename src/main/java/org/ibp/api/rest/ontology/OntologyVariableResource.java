package org.ibp.api.rest.ontology;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.GenericResponse;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.domain.ontology.VariableRequest;
import org.ibp.api.domain.ontology.VariableResponse;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.VariableRequestValidator;
import org.ibp.api.java.ontology.OntologyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Variable Service")
@Controller
@RequestMapping("/ontology")
public class OntologyVariableResource {

	@Autowired
	private OntologyModelService ontologyModelService;

	@Autowired
	private RequestIdValidator requestIdValidator;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private ProgramValidator programValidator;

	@Autowired
	private VariableRequestValidator variableRequestValidator;

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable
	 *            types.
	 */
	@ApiOperation(value = "All variables", notes = "Gets all variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableSummary>> listAllVariables(@PathVariable String cropname,
			@RequestParam(value = "property", required = false) String propertyId,
			@RequestParam(value = "favourite", required = false) Boolean favourite,
			@RequestParam(value = "programId") String programId) throws MiddlewareQueryException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");
		this.programValidator.validate(programId, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		Integer pId = null;

		if (!Strings.isNullOrEmpty(propertyId)) {
			this.requestIdValidator.validate(propertyId, bindingResult);
			if (bindingResult.hasErrors()) {
				throw new ApiRequestValidationException(bindingResult.getAllErrors());
			}
			pId = Integer.valueOf(propertyId);
		}
		return new ResponseEntity<>(this.ontologyModelService.getAllVariablesByFilter(Integer.valueOf(programId), pId, favourite), HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable
	 *            types.
	 */
	@ApiOperation(value = "Get Variable", notes = "Get Variable By Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<VariableResponse> getVariableById(@PathVariable String cropname,
															@RequestParam(value = "programId") String programId, @PathVariable String id)
			throws MiddlewareQueryException, MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Variable");
		this.programValidator.validate(programId, bindingResult);
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		TermRequest request = new TermRequest(Integer.valueOf(id), "variable",
				CvId.VARIABLES.getId());
		this.termValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyModelService.getVariableById(
				Integer.valueOf(programId), Integer.valueOf(id)), HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable
	 *            types.
	 */
	@ApiOperation(value = "Add Variable", notes = "Add new variable using given data")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addVariable(@PathVariable String cropname,
													   @RequestBody VariableRequest request, BindingResult bindingResult)
					throws MiddlewareQueryException, MiddlewareException {
		this.variableRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyModelService.addVariable(request), HttpStatus.CREATED);
	}

}
