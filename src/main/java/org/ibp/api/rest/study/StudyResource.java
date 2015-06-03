package org.ibp.api.rest.study;

import java.util.List;

import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.java.study.StudyService;
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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Study Services")
@Controller
@RequestMapping("/study")
public class StudyResource {

	@Autowired
	private StudyService studyService;

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	@ApiOperation(value = "List all studies", notes = "Returns summary information for all studies (Nurseries and Trials).")
	@RequestMapping(value = "/{cropname}/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudySummary>> listAllStudies(@PathVariable String cropname, @RequestParam(value = "programUniqueId", required = false) String programUniqueId) {
		return new ResponseEntity<>(this.studyService.listAllStudies(programUniqueId), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Get all observations", notes = "Returns observations available in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/observations", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Observation>> getObservations(@PathVariable String cropname, @PathVariable Integer studyId) {
		return new ResponseEntity<>(studyService.getObservations(studyId), HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "Get a observations", notes = "Returns the requested observation in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/observations/{observationId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Observation> getSingleObservation(@PathVariable String cropname, @PathVariable Integer studyId, @PathVariable Integer observationId) {
		return new ResponseEntity<>(studyService.getSingleObservation(studyId, observationId), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Update an observation", notes = "Returns observations available in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/observations/{observationId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Observation> updateObservation(@PathVariable String cropname, @PathVariable Integer studyId, @PathVariable Integer observationId,
			@RequestBody Observation observation) {
		if(observation.getUniqueIdentifier() != observationId) {
			throw new IllegalArgumentException("The observation identifier must be the same in the object an the url");
			//TODO: Give back some better error messages.
		}
		return new ResponseEntity<>(studyService.updateObsevation(studyId, observation), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Get Study Germplasm List", notes = "Returns a list of germplasm used in the study.")
	@RequestMapping(value = "/{cropname}/{studyId}/germplasm", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyGermplasm>> getStudyGermplasm(@PathVariable String cropname, @PathVariable Integer studyId) {
		return new ResponseEntity<List<StudyGermplasm>>(studyService.getStudyGermplasmList(studyId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Study Details", notes = "Returns detailed information about the study.")
	@RequestMapping(value = "/{cropname}/{studyId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudyDetails> getStudyDetails(@PathVariable String cropname, @PathVariable String studyId) {
		return new ResponseEntity<StudyDetails>(this.studyService.getStudyDetails(studyId), HttpStatus.OK);
	}

}
