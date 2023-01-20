package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceSearchRequest;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.java.study.GermplasmStudySourceService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Study Germplasm Harvest Services")
@Controller
@RequestMapping("/crops")
public class GermplasmStudySourceResource {

	@Resource
	private GermplasmStudySourceService germplasmStudySourceService;

	@Autowired
	private SearchRequestService searchRequestService;

	@ApiOperation(value = "It will retrieve all generated germplasm in a study",
		notes = "It will retrieve all generated germplasm in a study")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES')")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/germplasm-sources/table", method = RequestMethod.POST)
	@ResponseBody
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	public ResponseEntity<List<GermplasmStudySourceDto>> getGermplasmStudySourceTable(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestBody final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest,
		final @ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) Pageable pageable) {

		germplasmStudySourceSearchRequest.setStudyId(studyId);
		return new PaginatedSearch()
			.getPagedResult(() -> this.germplasmStudySourceService.countGermplasmStudySources(germplasmStudySourceSearchRequest),
				() -> this.germplasmStudySourceService.countFilteredGermplasmStudySources(germplasmStudySourceSearchRequest),
				() -> this.germplasmStudySourceService.getGermplasmStudySources(germplasmStudySourceSearchRequest, pageable), pageable
			);
	}


	@ApiOperation(value = "Post generated germplasm in a study search", notes = "Post generated germplasm in a study search")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/germplasm-sources/search", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES')")
	@ResponseBody
	public ResponseEntity<SearchDto> postGermplasmStudySourceTable(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestBody final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest
	) {
		final String searchRequestId =
			this.searchRequestService.saveSearchRequest(germplasmStudySourceSearchRequest, GermplasmStudySourceSearchRequest.class).toString();
		return new ResponseEntity<>(new SearchDto(searchRequestId), HttpStatus.OK);

	}

}
