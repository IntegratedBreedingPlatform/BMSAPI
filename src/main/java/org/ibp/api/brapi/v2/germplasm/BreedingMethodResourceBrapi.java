package org.ibp.api.brapi.v2.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Api(value = "BrAPI Germplasm Services")
@Controller
public class BreedingMethodResourceBrapi {

	@Autowired
	private BreedingMethodService breedingMethodService;

	@ApiOperation(value = "Get All breeding methods", notes = "Get All Breeding Methods. ")
	@ResponseBody
	public ResponseEntity<EntityListResponse<BreedingMethod>> getAllBreedingMethods(@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
	@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

		final PagedResult<BreedingMethod> resultPage = new PaginatedSearch().executeBrapiSearch(currentPage, pageSize,
			new SearchSpec<BreedingMethod>() {

				@Override
				public long getCount() {
					return breedingMethodService.getAllBreedingMethods().size();
				}

				@Override
				public List<BreedingMethod> getResults(final PagedResult<BreedingMethod> pagedResult) {
					List<BreedingMethod> results = new ArrayList<>();
					List<BreedingMethodDTO> breedingMethodDTOS = breedingMethodService.getAllBreedingMethods();
					for (final BreedingMethodDTO dto : breedingMethodDTOS) {
						BreedingMethod method = new BreedingMethod();
						method.setAbbreviation(dto.getCode());
						method.setBreedingMethodDbId(String.valueOf(dto.getMethodClass()));
						method.setBreedingMethodName(dto.getName());
						method.setDescription(dto.getDescription());
						results.add(method);
					}
					return results;
				}
			});
		final Result<BreedingMethod> result = new Result<BreedingMethod>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());
		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<BreedingMethod> entityListResponse = new EntityListResponse<BreedingMethod>(metadata, result);
		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}
}
