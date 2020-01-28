package org.ibp.api.brapi.v1.variable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.service.api.study.VariableDto;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "BrAPI Variable Services")
@Controller
public class VariableResourceBrapi {

	@Autowired
	private StudyService studyService;

	@ApiOperation(value = "Call to retrieve a list of observation variables available in the system.")
	@RequestMapping(value = "/{crop}/brapi/v1/variables", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<VariableDto>> getAllVariables(final HttpServletResponse response,
		@PathVariable final String crop,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final PagedResult<VariableDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<VariableDto>() {

				@Override
				public long getCount() {
					return VariableResourceBrapi.this.studyService.countVariables();
				}

				@Override
				public List<VariableDto> getResults(final PagedResult<VariableDto> pagedResult) {
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return VariableResourceBrapi.this.studyService
						.getVariables(pagedResult.getPageSize(), pageNumber, crop);
				}
			});

		final List<VariableDto> observationVariables = resultPage.getPageResults();

		final Result<VariableDto> result = new Result<VariableDto>().withData(observationVariables);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<VariableDto> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

}
