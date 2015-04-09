package org.ibp.api.rest.study;

import java.util.HashMap;
import java.util.List;

import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidator;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Study Services")
@Controller
@RequestMapping("/study")
public class StudyResource {

	@Autowired
	private CropNameValidator cropNameValidator;


	@Autowired
	private StudyService studyService;

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	@ApiOperation(value = "List all studies", notes = "Returns summary information for all studies (Nurseries and Trials).")
	@RequestMapping(value = "/{cropname}/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudySummary>> listAllStudies(@PathVariable String cropname) {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.cropNameValidator.validate(cropname, bindingResult);
		if(bindingResult.hasErrors()){
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		return new ResponseEntity<>(this.studyService.listAllStudies(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Get all observations", notes = "Returns observations available in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/observations", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Observation>> getObservations(@PathVariable String cropname, @PathVariable Integer studyId) {
		return new ResponseEntity<>(studyService.getObservations(studyId), HttpStatus.OK);
	}

}
