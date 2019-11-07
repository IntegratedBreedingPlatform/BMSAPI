
package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(value = "Study Services")
@Controller
public class StudyResource {

	private static final String NO_PERMISSION_FOR_LOCKED_STUDY = "no.permission.for.locked.study";

	@Autowired
	private StudyService studyService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private SecurityService securityService;

	private static final Logger LOG = LoggerFactory.getLogger(StudyResource.class);

	@ApiOperation(value = "List all study instances with basic metadata.",
			notes = "Returns list of all study instances with basic metadata.")
	@RequestMapping(value = "/study/{cropname}/{studyId}/instances", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyInstance>> listStudyInstances(final @PathVariable String cropname,
			@PathVariable final Integer studyId) {
		final List<StudyInstance> studyInstances = this.studyService.getStudyInstances(studyId);

		if (studyInstances.isEmpty()) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<List<StudyInstance>>(studyInstances, HttpStatus.OK);
	}

	@ApiOperation(value = "Check if a study is sampled.",
			notes = "Returns boolean indicating if there are samples associated to the study.")
	@RequestMapping(value = "/study/{cropName}/{studyId}/sampled", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Boolean> hasSamples(final @PathVariable String cropName,
			@PathVariable final Integer studyId) {
		final Boolean hasSamples = this.studyService.isSampled(studyId);
		return new ResponseEntity<>(hasSamples, HttpStatus.OK);
	}

	@ApiOperation(value = "Partially modifies a study",
			notes = "As of now, it only allows to update the status")
	@RequestMapping(value = "/study/{cropName}/{studyId}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Void> patchStudy (final @PathVariable String cropName,
			@PathVariable final Integer studyId, @RequestBody final Study study) {
		// TODO Properly define study entity, Identify which attributes of the Study entity can be updated, Implement patch accordingly
		study.setId(studyId);
		this.studyService.updateStudy(study);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
