package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.dataset.StockPropertyData;
import org.ibp.api.java.study.StudyEntryObservationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Arrays;

@Api(value = "Study Entry Observation Services")
@Controller
public class StudyEntryObservationResource {

	@Resource
	private StudyEntryObservationService studyEntryObservationService;

	@ApiOperation(value = "Add new observation for a given study entry", notes = "Add new observation for a given study entry")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'MODIFY_ENTRY_DETAILS_VALUES')")
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
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'MODIFY_ENTRY_DETAILS_VALUES')")
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
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'MODIFY_ENTRY_DETAILS_VALUES')")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/observations/{observationId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteObservation(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer observationId) {
		this.studyEntryObservationService.deleteObservation(studyId, observationId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Count study entry observations", notes = "Returns count of study entry observations given a set of variables")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'GERMPLASM_AND_CHECKS', 'MODIFY_ENTRY_DETAILS_VALUES')")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/observations", method = RequestMethod.HEAD)
	public ResponseEntity<Void> countObservationsByVariables(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestParam(value = "variableIds") final Integer[] variableIds) {

		final long count = this.studyEntryObservationService.countObservationsByVariables(studyId, Arrays.asList(variableIds));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>(respHeaders, HttpStatus.OK);
	}

}
