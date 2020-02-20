package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.study.StudyEnvironmentService;
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
import java.util.List;
import java.util.Optional;

@Api(value = "Study Environment Services")
@Controller
@RequestMapping("/crops")
public class StudyEnvironmentResource {

	@Resource
	private StudyEnvironmentService studyEnvironmentService;

	@ApiOperation(value = "Create new study environments",
		notes = "Create new study environments")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/environments/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<StudyInstance>> createStudyEnvironments(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestParam final Integer numberOfEnvironmentsToGenerate) {
		return new ResponseEntity<>(this.studyEnvironmentService.createStudyEnvironments(cropname, studyId, numberOfEnvironmentsToGenerate),
			HttpStatus.OK);

	}

	@ApiOperation(value = "Delete study environments",
		notes = "Delete study environments")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/environments", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> deleteStudyEnvironments(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestParam final List<Integer> environmentIds) {
		this.studyEnvironmentService.deleteStudyEnvironments(studyId, environmentIds);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "List all study environments with basic metadata.",
		notes = "Returns list of all study environments with basic metadata.")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES', 'INFORMATION_MANAGEMENT', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/environments", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyInstance>> listStudyEnvironments(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId) {
		final List<StudyInstance> studyInstances = this.studyEnvironmentService.getStudyEnvironments(studyId);

		if (studyInstances.isEmpty()) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(studyInstances, HttpStatus.OK);
	}

	@ApiOperation(value = "Get study environment with basic metadata.",
		notes = "Get study environments with basic metadata.")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES', 'INFORMATION_MANAGEMENT', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/environments/{environmentId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudyInstance> getStudyInstance(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer environmentId) {
		final Optional<StudyInstance> studyInstance = this.studyEnvironmentService.getStudyEnvironment(studyId, environmentId);
		return studyInstance.isPresent()? new ResponseEntity<>(studyInstance.get(), HttpStatus.OK) : new ResponseEntity(HttpStatus.NOT_FOUND);
	}

}
