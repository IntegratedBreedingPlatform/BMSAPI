package org.ibp.api.rest.study;

import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.ibp.api.java.study.AdvanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

// TODO: add swagger
@Controller
// TODO: rename it!
public class NewAdvanceController {

	@Autowired
	private AdvanceService advanceService;

	// TODO: define permission
	@ResponseBody
	@RequestMapping(value = "/crops/{cropName}/studies/{studyId}/advance", method = RequestMethod.POST)
	public ResponseEntity<List<Integer>> advanceStudy(@PathVariable final String cropName, @PathVariable final Integer studyId,
		@RequestBody final AdvanceStudyRequest request) {

		final List<Integer> advancedGids = this.advanceService.advanceStudy(studyId, request);
		return new ResponseEntity<>(advancedGids, HttpStatus.OK);
	}

}
