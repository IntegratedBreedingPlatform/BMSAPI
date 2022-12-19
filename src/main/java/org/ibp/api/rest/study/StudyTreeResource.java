package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import org.ibp.api.java.study.StudyTreeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Api(value = "Study Tree Services")
@Controller
public class StudyTreeResource {

	@Resource
	private StudyTreeService studyTreeService;

	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{crop}/programs/{programUUID}/study-folders", method = RequestMethod.POST)
	public ResponseEntity<Integer> createStudyFolder(
		@PathVariable final String crop,
		@PathVariable final String programUUID,
		@RequestParam final String folderName,
		@RequestParam final Integer parentId) {

		return new ResponseEntity<>(this.studyTreeService.createStudyTreeFolder(crop, programUUID, parentId, folderName),
			HttpStatus.CREATED);

	}

}
