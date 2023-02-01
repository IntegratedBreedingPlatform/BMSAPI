package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "StudyType Services")
@Controller
@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES')")
@RequestMapping("/crops")
public class StudyTypeResource {

	@Autowired
	private StudyService studyService;

	@ApiOperation(value = "List all study type", notes = "Returns a list of all study types.")
	@RequestMapping(value = "/{cropname}/study-types/visible", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyTypeDto>> listStudyTypes(final @PathVariable String cropname, @RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.studyService.getStudyTypes(), HttpStatus.OK);
	}
}
