
package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

	@ApiOperation(value = "Check if a study is sampled.",
			notes = "Returns boolean indicating if there are samples associated to the study.")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/sampled", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES', 'INFORMATION_MANAGEMENT', 'BROWSE_STUDIES')")
	@ResponseBody
	public ResponseEntity<Boolean> hasSamples(final @PathVariable String cropName, @PathVariable final String programUUID,
			@PathVariable final Integer studyId) {
		final Boolean hasSamples = this.studyService.isSampled(studyId);
		return new ResponseEntity<>(hasSamples, HttpStatus.OK);
	}

	@ApiOperation(value = "Partially modifies a study",
			notes = "As of now, it only allows to update the status")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
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
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')" + PermissionsEnum.HAS_INVENTORY_VIEW)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getStudyTree(final @PathVariable String cropName,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId) {

		final List<TreeNode> studyTree = this.studyService.getStudyTree(parentFolderId, programUUID);
		return new ResponseEntity<>(studyTree, HttpStatus.OK);
	}

	@ApiOperation(value = "Get study entries",
		notes = "Get study entries as table")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property(,asc|desc). " +
				"Default sort order is ascending. " +
				"Multiple sort criteria are supported.")
	})
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/entries/table", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES')")
	@ResponseBody
	public ResponseEntity<List<StudyEntryDto>> getEntriesAsTable(final @PathVariable String cropName, @PathVariable final String programUUID,
		@PathVariable final Integer studyId, @ApiIgnore final Pageable pageable) {

		final PagedResult<StudyEntryDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<StudyEntryDto>() {

				@Override
				public long getCount() {
					return StudyResource.this.studyService.countAllStudyEntries(studyId);
				}

				@Override
				public List<StudyEntryDto> getResults(final PagedResult<StudyEntryDto> pagedResult) {
					return StudyResource.this.studyService.getStudyEntries(studyId, pageable);
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		headers.add("X-Total-Pages", Long.toString(resultPage.getTotalPages()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}

}
