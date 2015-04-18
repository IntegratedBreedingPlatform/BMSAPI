package org.ibp.api.rest.ontology;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.ontology.OntologyMapper;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.VariableRequestValidator;
import org.ibp.api.java.ontology.OntologyVariableService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * NOTE: Work in Progress, Do Not Use API Exposed
 */

@Api(value = "Ontology Variable Service")
@Controller
@RequestMapping("/ontology")
public class OntologyVariableResource {

	@Autowired
	private CropNameValidator cropNameValidator;

	@Autowired
	private OntologyVariableService ontologyVariableService;

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
	 *            The name of the crop which is we wish to retrieve variables.
	 */
	@ApiOperation(value = "All variables", notes = "Gets all variables.")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableSummary>> listAllVariables(@PathVariable String cropname,
																  @RequestParam(value = "property", required = false) String propertyId,
																  @RequestParam(value = "favourite", required = false) Boolean favourite,
																  @RequestParam(value = "programId") String programId) throws MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

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
		return new ResponseEntity<>(this.ontologyVariableService.getAllVariablesByFilter(programId, pId, favourite), HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to retrieve variable.
	 */
	@ApiOperation(value = "Get Variable", notes = "Get Variable By Id")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<VariableResponse> getVariableById(@PathVariable String cropname,
															@RequestParam(value = "programId") String programId,
															@PathVariable String id) throws MiddlewareException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.programValidator.validate(programId, bindingResult);
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		TermRequest request = new TermRequest(id, "variable", CvId.VARIABLES.getId());
		this.termValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyVariableService.getVariableById(programId, Integer.valueOf(id)), HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The name of the crop which is we wish to add variable.
	 */
	@ApiOperation(value = "Add Variable", notes = "Add new variable using given data")
	@RequestMapping(value = "/{cropname}/variables", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addVariable(@PathVariable String cropname, @RequestParam(value = "programId") String programId,
													   @RequestBody AddVariableRequest addVariableRequest) throws MiddlewareException {

		ModelMapper mapper = OntologyMapper.getInstance();
		VariableRequest request = mapper.map(addVariableRequest, VariableRequest.class);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.programValidator.validate(programId, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		request.setProgramUuid(programId);

		this.variableRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		return new ResponseEntity<>(this.ontologyVariableService.addVariable(request), HttpStatus.CREATED);
	}

	/**
	 *
	 * @param cropname The name of the crop which is we wish to add variable.
	 * @param programId programId to which variable is related
	 * @param id variable id
	 * @throws MiddlewareException
	 */
	@ApiOperation(value = "Update Variable", notes = "Update variable using given data")
	@RequestMapping(value = "/{cropname}/variables/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateVariable(@PathVariable String cropname,
										 @RequestParam(value = "programId") String programId,
										 @PathVariable String id,
										 @RequestBody UpdateVariableRequest updateVariableDetail) throws MiddlewareException {

		ModelMapper mapper = OntologyMapper.getInstance();
		VariableRequest request = mapper.map(updateVariableDetail, VariableRequest.class);

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.programValidator.validate(programId, bindingResult);
		this.requestIdValidator.validate(id, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		request.setId(id);
		request.setProgramUuid(programId);

		this.variableRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.ontologyVariableService.updateVariable(request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
