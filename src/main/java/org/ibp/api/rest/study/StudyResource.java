
package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.study.MyStudiesDTO;
import org.generationcp.middleware.api.study.MyStudiesService;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
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
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Study Services")
@Controller
@RequestMapping("/crops")
public class StudyResource {

	@Autowired
	private StudyService studyService;

	@Autowired
	private MyStudiesService myStudiesService;

	@Autowired
	private SecurityService securityService;

	@ApiOperation(value = "Check if a study is sampled.",
			notes = "Returns boolean indicating if there are samples associated to the study.")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/sampled", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'BROWSE_STUDIES')")
	@ResponseBody
	public ResponseEntity<Boolean> hasSamples(final @PathVariable String cropName, @PathVariable final String programUUID,
			@PathVariable final Integer studyId) {
		final Boolean hasSamples = this.studyService.isSampled(studyId);
		return new ResponseEntity<>(hasSamples, HttpStatus.OK);
	}

	@ApiOperation(value = "Partially modifies a study",
			notes = "As of now, it only allows to update the status")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@ResponseBody
	public ResponseEntity<Void> patchStudy (final @PathVariable String cropName, @PathVariable final String programUUID,
			@PathVariable final Integer studyId, @RequestBody final Study study) {
		// TODO Properly define study entity, Identify which attributes of the Study entity can be updated, Implement patch accordingly
		study.setId(studyId);
		this.studyService.updateStudy(study);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get the study tree")
	@RequestMapping(value = "/{cropName}/studies/tree", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')" + PermissionsEnum.HAS_INVENTORY_VIEW)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getStudyTree(final @PathVariable String cropName,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId) {

		final List<TreeNode> studyTree = this.studyService.getStudyTree(parentFolderId, programUUID);
		return new ResponseEntity<>(studyTree, HttpStatus.OK);
	}

	@ApiOperation("Get my studies along with statistical information")
	@RequestMapping(value = "/{cropName}/my-studies", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	@ResponseBody
	public ResponseEntity<List<MyStudiesDTO>> getMyStudies(
		@PathVariable final String cropName,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {
		final Integer userId = this.securityService.getCurrentlyLoggedInUser().getUserid();
		final PagedResult<MyStudiesDTO> result =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<MyStudiesDTO>() {

				@Override
				public long getCount() {
					return myStudiesService.countMyStudies(programUUID, userId);
				}

				@Override
				public List<MyStudiesDTO> getResults(final PagedResult<MyStudiesDTO> pagedResult) {
					return myStudiesService.getMyStudies(programUUID, pageable, userId);
				}
			});
		final List<MyStudiesDTO> pageResults = result.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(result.getTotalResults()));

		return new ResponseEntity<>(pageResults, headers, HttpStatus.OK );
	}

	@ApiOperation(value = "Delete a study", notes = "Delete a study")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@ResponseBody
	public ResponseEntity<Void> deleteStudy(final @PathVariable String cropName, @PathVariable final String programUUID,
		@PathVariable final Integer studyId) {
		this.studyService.deleteStudy(studyId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
