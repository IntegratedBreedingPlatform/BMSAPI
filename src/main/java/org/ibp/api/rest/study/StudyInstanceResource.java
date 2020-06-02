package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.InstanceData;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.study.StudyInstanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Api(value = "Study Instance Services")
@Controller
@RequestMapping("/crops")
public class StudyInstanceResource {

	@Resource
	private StudyInstanceService studyInstanceService;

	@ApiOperation(value = "Create new study instances",
		notes = "Create new study instances")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/instances/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<StudyInstance>> createStudyInstances(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestParam final Integer numberOfInstancesToGenerate) {
		return new ResponseEntity<>(this.studyInstanceService.createStudyInstances(cropname, studyId, numberOfInstancesToGenerate),
			HttpStatus.OK);

	}

	@ApiOperation(value = "Delete study instances",
		notes = "Delete study instances")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/instances", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> deleteStudyInstances(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestParam final List<Integer> instanceIds) {
		this.studyInstanceService.deleteStudyInstances(studyId, instanceIds);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "List all study instances with basic metadata.",
		notes = "Returns list of all study instances with basic metadata.")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES', 'INFORMATION_MANAGEMENT', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/instances", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyInstance>> listStudyEnvironments(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId) {
		final List<StudyInstance> studyInstances = this.studyInstanceService.getStudyInstances(studyId);

		if (studyInstances.isEmpty()) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(studyInstances, HttpStatus.OK);
	}

	@ApiOperation(value = "Get study instances with basic metadata.",
		notes = "Get study instances with basic metadata.")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES', 'INFORMATION_MANAGEMENT', 'BROWSE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/instances/{instanceId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudyInstance> getStudyInstance(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer instanceId) {
		final Optional<StudyInstance> studyInstance = this.studyInstanceService.getStudyInstance(studyId, instanceId);
		return studyInstance.isPresent()? new ResponseEntity<>(studyInstance.get(), HttpStatus.OK) : new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "Add study instance data",
		notes = "Add study environment data")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/instances/{instanceId}/instance-data", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<InstanceData> addInstanceData(final @PathVariable String cropname, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer instanceId,
		@RequestBody final InstanceData instanceData) {
		return new ResponseEntity<>(this.studyInstanceService.addInstanceData(studyId, instanceId, instanceData),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Update study instance data",
		notes = "Update study instance data")
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/instances/{instanceId}/instance-data/{instanceDataId}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<InstanceData> updateEnvironmentData(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer instanceId, @PathVariable final Integer instanceDataId,
		@RequestBody final InstanceData instanceData) {
		return new ResponseEntity<>(this.studyInstanceService
			.updateInstanceData(studyId, instanceId, instanceDataId, instanceData),
			HttpStatus.OK);
	}

}
