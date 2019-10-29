package org.ibp.api.rest.study;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.java.study.StudyInstanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Api(value = "Study Instance Services")
@Controller
@RequestMapping("/crops")
public class StudyInstanceResource {

	@Resource
	private StudyInstanceService studyInstanceService;

	@ApiOperation(value = "Create new study instance",
		notes = "Create new study instance")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/instance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity createStudyInstance(final @PathVariable String cropname,
		@PathVariable final Integer studyId, @PathVariable final Integer datasetId, @PathVariable final String instanceNumber) {

		this.studyInstanceService.createStudyInstance(cropname, datasetId, instanceNumber);

		return new ResponseEntity(HttpStatus.OK);

	}

	@ApiOperation(value = "Remove study instance",
		notes = "Remove instance from study")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/instance", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity removeStudyInstance(final @PathVariable String cropname,
		@PathVariable final Integer studyId, @PathVariable final Integer datasetId, @PathVariable final String instanceNumber) {

		this.studyInstanceService.removeStudyInstance(cropname, datasetId, instanceNumber);

		return new ResponseEntity(HttpStatus.OK);

	}

}
