package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.dataset.StockPropertyData;
import org.ibp.api.java.study.StudyEntryObservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

@Api(value = "Study Entry Observation Services")
@Controller
public class StudyEntryObservationResource {

	@Resource
	private StudyEntryObservationService studyEntryObservationService;

	@ApiOperation(value = "Add new observation for a given study entry", notes = "Add new observation for a given study entry")
	// TODO: review permissions
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/observations", method = RequestMethod.POST)
	public ResponseEntity<Integer> createObservation(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody final StockPropertyData stockPropertyData) {
		final Integer stockPropertyId = this.studyEntryObservationService.createObservation(studyId, stockPropertyData);
		return new ResponseEntity<>(stockPropertyId, HttpStatus.OK);
	}

	@ApiOperation(value = "Updates the given study entry observation", notes = "Updates the given study entry observation")
	// TODO: review permissions
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/observations", method = RequestMethod.PATCH)
	public ResponseEntity<Integer> updateObservation(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody final StockPropertyData stockPropertyData) {
		final Integer stockPropertyId = this.studyEntryObservationService.updateObservation(studyId, stockPropertyData);
		return new ResponseEntity<>(stockPropertyId, HttpStatus.OK);
	}

	@ApiOperation(value = "Deletes the given study entry observation", notes = "Deletes the given study entry observation")
	// TODO: review permissions
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/observations/{observationId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteObservation(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer observationId) {
		this.studyEntryObservationService.deleteObservation(studyId, observationId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
