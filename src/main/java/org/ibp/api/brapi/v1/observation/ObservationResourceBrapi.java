package org.ibp.api.brapi.v1.observation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.dataset.DatasetTypeService;
import org.ibp.api.java.impl.middleware.permission.validator.BrapiPermissionValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "BrAPI Observation Services")
@Controller
public class ObservationResourceBrapi {

	@Autowired
	private DatasetTypeService datasetTypeService;

	@Autowired
	private BrapiPermissionValidator permissionValidator;

	@ApiOperation(value = "Get observation levels", notes = "Returns a list of supported observation levels")
	@RequestMapping(value = "/{crop}/brapi/v1/observationlevels", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<String>> getObservationLevels(
		@PathVariable final String crop,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize
	) {

		this.permissionValidator.validatePermissions(crop, "ADMIN", "STUDIES", "MANAGE_STUDIES");

		final PagedResult<String> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<String>() {

				@Override
				public long getCount() {
					return ObservationResourceBrapi.this.datasetTypeService.countObservationLevels();
				}

				@Override
				public List<String> getResults(final PagedResult<String> pagedResult) {
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return ObservationResourceBrapi.this.datasetTypeService
						.getObservationLevels(pagedResult.getPageSize(), pageNumber);
				}
			});

		final List<String> observationLevels = resultPage.getPageResults();

		final Result<String> results = new Result<String>().withData(observationLevels);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<String> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

}
