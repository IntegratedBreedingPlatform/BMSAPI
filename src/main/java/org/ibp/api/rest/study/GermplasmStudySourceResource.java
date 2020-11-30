package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceSearchRequest;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.GermplasmStudySourceService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
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

	@ApiOperation(value = "It will retrieve all generated germplasm in a study",
		notes = "It will retrieve all generated germplasm in a study")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'BROWSE_STUDIES')"
		+ PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
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

		final PagedResult<GermplasmStudySourceDto> pageResult =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmStudySourceDto>() {

				@Override
				public long getCount() {
					return GermplasmStudySourceResource.this.germplasmStudySourceService
						.countGermplasmStudySources(germplasmStudySourceSearchRequest);
				}

				@Override
				public long getFilteredCount() {
					return GermplasmStudySourceResource.this.germplasmStudySourceService
						.countFilteredGermplasmStudySources(germplasmStudySourceSearchRequest);
				}

				@Override
				public List<GermplasmStudySourceDto> getResults(final PagedResult<GermplasmStudySourceDto> pagedResult) {
					return GermplasmStudySourceResource.this.germplasmStudySourceService
						.getGermplasmStudySources(germplasmStudySourceSearchRequest, pageable);
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Filtered-Count", Long.toString(pageResult.getFilteredResults()));
		headers.add("X-Total-Count", Long.toString(pageResult.getTotalResults()));
		return new ResponseEntity<>(pageResult.getPageResults(), headers, HttpStatus.OK);

	}

}
