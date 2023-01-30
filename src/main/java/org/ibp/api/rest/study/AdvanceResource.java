package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.study.AdvanceSamplesRequest;
import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.ibp.api.java.study.AdvanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Advance Services")
@Controller
public class AdvanceResource {

	@Autowired
	private AdvanceService advanceService;

	@ApiOperation(value = "Advance study")
	// TODO: define granular permission for advance
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@ResponseBody
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/advance", method = RequestMethod.POST)
	public ResponseEntity<List<Integer>> advanceStudy(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody final AdvanceStudyRequest request) {

		final List<Integer> advancedGids = this.advanceService.advanceStudy(studyId, request);
		return new ResponseEntity<>(advancedGids, HttpStatus.OK);
	}

	@ApiOperation(value = "Advance sampled plants from plots")
	// TODO: define granular permission for advance
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@ResponseBody
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/advance/samples", method = RequestMethod.POST)
	public ResponseEntity<List<Integer>> advanceSamples(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody final AdvanceSamplesRequest request) {

		final List<Integer> advancedGids = this.advanceService.advanceSamples(studyId, request);
		return new ResponseEntity<>(advancedGids, HttpStatus.OK);
	}

}
