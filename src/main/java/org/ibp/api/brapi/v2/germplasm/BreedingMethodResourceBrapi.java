package org.ibp.api.brapi.v2.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Api(value = "BrAPI Germplasm Services")
@Controller
public class BreedingMethodResourceBrapi {

	@Autowired
	private BreedingMethodService breedingMethodService;

	@ApiOperation(value = "Get the Breeding Methods", notes = "Get the list of germplasm breeding methods available in a system.")
	@RequestMapping(value = "/{crop}/brapi/v2/breedingmethods", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<BreedingMethod>> getAllBreedingMethods(@PathVariable final String crop, @ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
	@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest();
		final PagedResult<BreedingMethodDTO> resultPage = new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize,
			new SearchSpec<BreedingMethodDTO>() {

				@Override
				public long getCount() {
					return BreedingMethodResourceBrapi.this.breedingMethodService.countBreedingMethods(searchRequest);
				}

				@Override
				public List<BreedingMethodDTO> getResults(final PagedResult<BreedingMethodDTO> pagedResult) {
					return BreedingMethodResourceBrapi.this.breedingMethodService.getBreedingMethods(crop, searchRequest, new PageRequest(finalPageNumber, finalPageSize));
				}
			});

		final ModelMapper modelMapper = BreedingMethodMapper.getInstance();
		final List<BreedingMethod> breedingMethods = new ArrayList<>();
		for (final BreedingMethodDTO dto : resultPage.getPageResults()) {
			breedingMethods.add(modelMapper.map(dto, BreedingMethod.class));
		}
		final Result<BreedingMethod> result = new Result<BreedingMethod>().withData(breedingMethods);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());
		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<BreedingMethod> entityListResponse = new EntityListResponse<BreedingMethod>(metadata, result);
		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get the details of a specific Breeding Method used to produce Germplasm", notes = "Get the details of a specific Breeding Method")
	@RequestMapping(value = "/{crop}/brapi/v2/breedingmethod/{breedingMethodDbId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<BreedingMethod>> getBreedingMethodById(
		@PathVariable final String crop,
		@PathVariable final Integer breedingMethodDbId) {

		final BreedingMethodDTO breedingMethodDTOS = this.breedingMethodService.getBreedingMethod(breedingMethodDbId);
		if (Objects.isNull(breedingMethodDTOS)) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());;
			errors.reject("methoddbid.invalid", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final ModelMapper modelMapper = BreedingMethodMapper.getInstance();
		final BreedingMethod method = modelMapper.map(breedingMethodDTOS, BreedingMethod.class);
		final Metadata metadata = new Metadata();
		final Pagination pagination = new Pagination().withPageNumber(1).withPageSize(1).withTotalCount(1L).withTotalPages(1);
		metadata.setPagination(pagination);
		metadata.setStatus(Collections.singletonList(new HashMap<>()));
		final SingleEntityResponse<BreedingMethod> entityListResponse = new SingleEntityResponse<BreedingMethod>(metadata, method);
		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}
}
