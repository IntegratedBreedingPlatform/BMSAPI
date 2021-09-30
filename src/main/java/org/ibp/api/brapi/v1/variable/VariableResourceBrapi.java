package org.ibp.api.brapi.v1.variable;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.ontology.VariableService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Api(value = "BrAPI Variable Services")
@Controller
public class VariableResourceBrapi {

	@Autowired
	private VariableService variableService;

	@ApiOperation(value = "Call to retrieve a list of observation variables available in the system.")
	@RequestMapping(value = "/{crop}/brapi/v1/variables", method = RequestMethod.GET)
	@JsonView(BrapiView.BrapiV1_3.class)
	@ResponseBody
	public ResponseEntity<EntityListResponse<VariableDTO>> getAllVariables(final HttpServletResponse response,
		@PathVariable final String crop,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final PagedResult<VariableDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<VariableDTO>() {

				@Override
				public long getCount() {
					return VariableResourceBrapi.this.variableService.countAllVariables(Collections.unmodifiableList(
						Arrays.asList(VariableType.TRAIT.getId())));
				}

				@Override
				public List<VariableDTO> getResults(final PagedResult<VariableDTO> pagedResult) {
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return VariableResourceBrapi.this.variableService
						.getAllVariables(crop, Collections.unmodifiableList(
							Arrays.asList(VariableType.TRAIT.getId())), pagedResult.getPageSize(), pageNumber);
				}
			});

		final List<VariableDTO> observationVariables = resultPage.getPageResults();

		final Result<VariableDTO> result = new Result<VariableDTO>().withData(observationVariables);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<VariableDTO> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

}
