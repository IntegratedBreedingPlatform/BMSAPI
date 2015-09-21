
package org.ibp.api.rest.study;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.StudyWorkbook;
import org.ibp.api.java.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Study Services")
@Controller
@RequestMapping("/study")
public class StudyResource {

	@Autowired
	private StudyService studyService;
	
	private Logger LOGGER = LoggerFactory.getLogger(StudyResource.class);

	/**
	 * @param cropname The crop for which this rest call is being made
	 */
	@ApiOperation(value = "List all studies", notes = "Returns summary information for all studies (Nurseries and Trials).")
	@RequestMapping(value = "/{cropname}/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudySummary>> listAllStudies(@PathVariable String cropname, @RequestParam(value = "programUniqueId",
			required = false) String programUniqueId) {
		return new ResponseEntity<>(this.studyService.listAllStudies(programUniqueId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get all observations", notes = "Returns observations available in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/observations", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Observation>> getObservations(@PathVariable String cropname, @PathVariable Integer studyId) {
		return new ResponseEntity<>(this.studyService.getObservations(studyId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get a observations", notes = "Returns the requested observation in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/observations/{observationId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Observation> getSingleObservation(@PathVariable String cropname, @PathVariable Integer studyId,
			@PathVariable Integer observationId) {
		return new ResponseEntity<>(this.studyService.getSingleObservation(studyId, observationId), HttpStatus.OK);
	}

	@ApiOperation(value = "Update an observation", notes = "Returns observations available in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/observations/{observationId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Observation> updateObservation(@PathVariable String cropname, @PathVariable Integer studyId,
			@PathVariable Integer observationId, @RequestBody Observation observation) {
		if (observationId == null || observation.getUniqueIdentifier() == null || !observationId.equals(observation.getUniqueIdentifier() )) {
			throw new IllegalArgumentException("The observation identifier must be populated and have the same value in the object and the url");
			// TODO: Give back some better error messages.
		}
		return new ResponseEntity<>(this.studyService.updateObsevation(studyId, observation), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Study Germplasm List", notes = "Returns a list of germplasm used in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/germplasm", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyGermplasm>> getStudyGermplasm(@PathVariable String cropname, @PathVariable Integer studyId) {
		return new ResponseEntity<List<StudyGermplasm>>(this.studyService.getStudyGermplasmList(studyId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Study Details", notes = "Returns detailed information about the study.")
	@RequestMapping(value = "/{cropname}/{studyId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudyDetails> getStudyDetails(@PathVariable String cropname, @PathVariable String studyId) {
		return new ResponseEntity<StudyDetails>(this.studyService.getStudyDetails(studyId), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{cropname}/fieldmaps/{studyId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<Integer, FieldMap>> getFieldMap(@PathVariable String cropname, @PathVariable String studyId) {
		return new ResponseEntity<Map<Integer, FieldMap>>(this.studyService.getFieldMap(studyId), HttpStatus.OK);
	}

	@ApiOperation(value = "Load a study", notes = "Uploads one study (Nursery, Trial, etc) along with its dependendencies.")
	@RequestMapping(value = "/{cropname}/{program}/", method = RequestMethod.POST)
	public ResponseEntity<String> saveStudy(final @PathVariable String cropname,
			@PathVariable(value = "program") final String programUUID,
			@RequestBody @Valid final StudyWorkbook studyWorkbook,
			BindingResult bindingResult) {
		
		if(bindingResult.hasErrors()){
			String error = getErrorsAsString(bindingResult);
			
			LOGGER.error(error);
			throw new ValidationException(error);
		}
		Integer studyId = studyService.addNewStudy(studyWorkbook, programUUID);
		
		return new ResponseEntity<String>("{studyId: " + studyId + "}", HttpStatus.OK);
	}
	
	
	private String getErrorsAsString(final BindingResult bindingResult){
		StringBuilder validationErrors = new StringBuilder();
		for(FieldError error : bindingResult.getFieldErrors()){
			validationErrors
			.append("[")
			.append(error.getField())
			.append(": ")
			.append(error.getDefaultMessage())
			.append("]");
		}
		
		return validationErrors.toString();

	}

}
