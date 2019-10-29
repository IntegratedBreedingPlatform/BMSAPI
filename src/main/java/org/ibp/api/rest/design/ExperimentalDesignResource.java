package org.ibp.api.rest.design;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.java.design.ExperimentalDesignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

@Api(value = "Experimental Design Service")
@Controller
@RequestMapping("/crops")
public class ExperimentalDesignResource {

	@Resource
	private ExperimentalDesignService experimentalDesignService;

	@ApiOperation(value = "Generate experimental design for study", notes = "Generate experimental design for study")
	@RequestMapping(value = "/{crop}/studies/{studyId}/experimental-designs/generation", method = RequestMethod.POST)
	public ResponseEntity generateStudyExperimentDesign(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@RequestBody final ExperimentalDesignInput experimentalDesignInput) {

		this.experimentalDesignService.generateAndSaveDesign(crop, studyId, experimentalDesignInput);

		return new ResponseEntity<>(HttpStatus.OK);
	}


	@ApiOperation(value = "Delete experimental design of study", notes = "Delete experimental design of study")
	@RequestMapping(value = "/{crop}/studies/{studyId}/experimental-designs", method = RequestMethod.DELETE)
	public ResponseEntity generateStudyExperimentDesign(@PathVariable final String crop,
		@PathVariable final Integer studyId) {
		this.experimentalDesignService.deleteDesign(studyId);

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
