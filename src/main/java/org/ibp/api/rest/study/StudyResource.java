
package org.ibp.api.rest.study;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.lang3.ObjectUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyFolder;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
	
	@ApiOperation(value = "Search studies",
			notes = "Search studies (Nurseries and Trials) by various criteria (see parameter documentation).")
	@RequestMapping(value = "/study/{cropname}/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudySummary>> search(
			@ApiParam(value = "Required parameter. Must specify the crop database to query. "
					+ "Use <code>GET /crop/list</code> service to retrieve possible crop name values that can be supplied here.")
			@PathVariable final String cropname, //
			@ApiParam(
					value = "Optional parameter. "
							+ "If provided the results are filtered to only return studies that belong to the program identified by this unique id.")//
			@RequestParam(value = "programUniqueId", required = false) final String programUniqueId,

			@ApiParam(
					value = "Optional parameter. If provided the results are filtered to only return studies with specified principal investigator. "
							+ "Matches against value of study level attribute identified by ontology term id 8100 (PI_NAME).")//
			@RequestParam(value = "principalInvestigator", required = false) final String principalInvestigator,

			@ApiParam(value = "Optional parameter. If provided the results are filtered to only return studies at the specified location. "
					+ "Matches against value of study level attribute identified by ontology term id 8180 (LOCATION_NAME).")//
			@RequestParam(value = "location", required = false) final String location,

			@ApiParam(
					value = "Optional parameter. If provided the results are filtered to only return studies in conducted in specified season."
							+ "Matches against value of study level attribute identified by ontology term id 8370 (CROP_SEASON).")//
			@RequestParam(value = "season", required = false) final String season) {

		return new ResponseEntity<>(this.studyService.search(programUniqueId, cropname, principalInvestigator, location, season),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Get all observations", notes = "Returns observations available in the study.")
	@RequestMapping(value = "/study/{cropname}/{studyId}/observations", method = RequestMethod.GET)
	@ResponseBody
	@Transactional
	public ResponseEntity<PagedResult<Observation>> getObservations(@PathVariable final String cropname, //
			@PathVariable final Integer studyId, //
			@ApiParam(
					value = "One study can have multiple instances. Supply the instance number for which the observations need to be retrieved."
							+ " Use <code>GET /study/{cropname}/{studyId}/instances</code> service to retrieve a list of instances with instanceId and basic metadata.") //
			@RequestParam(value = "instanceId") final Integer instanceId, //
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.",
					required = false) //
			@RequestParam(value = "pageNumber", required = false) final Integer pageNumber, //
			@ApiParam(value = "Number of results to retrieve per page. Defaults to " + PagedResult.DEFAULT_PAGE_SIZE + " if not supplied. Max page size allowed is 10000.", required = false) //
			@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

		final PagedResult<Observation> pageResult = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<Observation>() {

			@Override
			public long getCount() {
				return StudyResource.this.studyService.countTotalObservationUnits(studyId, instanceId);
			}

			@Override
			public List<Observation> getResults(final PagedResult<Observation> pagedResult) {
				return StudyResource.this.studyService.getObservations(studyId, instanceId, pagedResult.getPageNumber(),
						pagedResult.getPageSize(), pagedResult.getSortBy(), pagedResult.getSortOrder());
			}
		});
		return new ResponseEntity<>(pageResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Get a observations", notes = "Returns the requested observation in the study.")
	@RequestMapping(value = "/study/{cropname}/{studyId}/observations/{observationId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Observation> getSingleObservation(@PathVariable final String cropname, @PathVariable final Integer studyId,
			@PathVariable final Integer observationId) {
		return new ResponseEntity<>(this.studyService.getSingleObservation(studyId, observationId), HttpStatus.OK);
	}

	@ApiOperation(value = "Update an observation", notes = "Returns observations available in the study.")
	@RequestMapping(value = "/study/{cropname}/{studyId}/observations/{observationId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Observation> updateObservation(@PathVariable final String cropname, @PathVariable final Integer studyId,
			@PathVariable final Integer observationId, @RequestBody final Observation observation) {
		if (observationId == null || observation.getUniqueIdentifier() == null || !observationId.equals(observation.getUniqueIdentifier())) {
			// TODO: Give back some better error messages.
			throw new IllegalArgumentException(
					"The observation identifier must be populated and have the same value in the object and the url");
			
		} else {
			final StudyReference study = this.studyService.getStudyReference(studyId);
			final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

			if (study != null && study.getIsLocked()
				&& !ObjectUtils.equals(study.getOwnerId(), loggedInUser.getUserid())
				&& !request.isUserInRole(Role.SUPERADMIN)) {
				throw new IllegalArgumentException(
						this.resourceBundleMessageSource.getMessage(NO_PERMISSION_FOR_LOCKED_STUDY,  new String[] {study.getOwnerName()}, LocaleContextHolder.getLocale()));
			}
		}
		return new ResponseEntity<>(this.studyService.updateObservation(studyId, observation), HttpStatus.OK);
	}

	@ApiOperation(value = "Add or update multiple observations", notes = "Returns observations added/updated.")
	@RequestMapping(value = "/study/{cropname}/{studyId}/observations", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<List<Observation>> addOrUpdateMultipleObservations(@PathVariable final String cropname,
			@PathVariable final Integer studyId, @RequestBody final List<Observation> observation) {
		final StudyReference study = this.studyService.getStudyReference(studyId);

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

		if (study.getIsLocked()
			&& !ObjectUtils.equals(study.getOwnerId(), loggedInUser.getUserid())
			&& !request.isUserInRole(Role.SUPERADMIN)) {
			throw new IllegalArgumentException(
					this.resourceBundleMessageSource.getMessage(NO_PERMISSION_FOR_LOCKED_STUDY, new String[] {study.getOwnerName()}, LocaleContextHolder.getLocale()));
		}
		return new ResponseEntity<>(this.studyService.updateObservations(studyId, observation), HttpStatus.OK);
	}


	@ApiOperation(value = "Get Study Germplasm List", notes = "Returns a list of germplasm used in the study.")
	@RequestMapping(value = "/study/{cropname}/{studyId}/germplasm", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyGermplasm>> getStudyGermplasm(@PathVariable final String cropname, @PathVariable final Integer studyId) {
		return new ResponseEntity<List<StudyGermplasm>>(this.studyService.getStudyGermplasmList(studyId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Study Details", notes = "Returns detailed information about the study.")
	@RequestMapping(value = "/study/{cropname}/{studyId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudyDetails> getStudyDetails(@PathVariable final String cropname, @PathVariable final String studyId) {
		return new ResponseEntity<StudyDetails>(this.studyService.getStudyDetails(studyId), HttpStatus.OK);
	}

	@RequestMapping(value = "/study/{cropname}/fieldmaps/{studyId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<Integer, FieldMap>> getFieldMap(@PathVariable final String cropname, @PathVariable final String studyId) {
		return new ResponseEntity<Map<Integer, FieldMap>>(this.studyService.getFieldMap(studyId), HttpStatus.OK);
	}

	private String getErrorsAsString(final BindingResult bindingResult) {
		final StringBuilder validationErrors = new StringBuilder();
		for (final FieldError error : bindingResult.getFieldErrors()) {
			validationErrors.append("[").append(error.getField()).append(": ").append(error.getDefaultMessage()).append("]");
		}
		return validationErrors.toString();
	}

	@ApiOperation(
			value = "List all study folders",
			notes = "Returns a flat list (no tree structure) of all study folders. The parentFolderId could be used to build a tree if needed.")
	@RequestMapping(value = "/study/{cropname}/folders", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyFolder>> listAllFolders(final @PathVariable String cropname) {
		return new ResponseEntity<List<StudyFolder>>(this.studyService.getAllStudyFolders(), HttpStatus.OK);
	}

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
			@PathVariable final Integer studyId, @RequestBody Study study) {
		// TODO Properly define study entity, Identify which attributes of the Study entity can be updated, Implement patch accordingly
		study.setId(studyId);
		this.studyService.updateStudy(study);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
