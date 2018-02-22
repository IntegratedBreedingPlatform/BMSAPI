package org.ibp.api.rest.study;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "StudyType Services")
@Controller
public class StudyTypeResource {

	@Autowired
	private StudyService studyService;

	@ApiOperation(value = "List all study type", notes = "Returns a list of all study types.")
	@RequestMapping(value = "/{cropname}/studytypes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyTypeDto>> listStudyTypes(final @PathVariable String cropname) {
		return new ResponseEntity<>(this.studyService.getStudyTypes(), HttpStatus.OK);
	}
}
