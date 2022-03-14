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
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/observations", method = RequestMethod.POST)
	public ResponseEntity<Integer> createStudyListObservation(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final StockPropertyData stockPropertyData) {
		final Integer stockPropertyId = this.studyEntryObservationService.createObservation(programUUID, studyId, datasetId, stockPropertyData);
		return new ResponseEntity<>(stockPropertyId, HttpStatus.OK);
	}


}
