package org.ibp.api.brapi.v1.phenotype;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "BrAPI Phenotype Services")
@Controller
public class PhenotypeSearchResource {

	@Autowired
	private ObservationUnitService observationUnitService;

	@ApiOperation(value = "Phenotype search", notes = "Returns a list of observationUnit with the observed Phenotypes")
	@RequestMapping(value = "/{crop}/brapi/v1/phenotypes-search", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV1_2.class)
	public ResponseEntity<EntityListResponse<ObservationUnitDto>> searchPhenotypes(@PathVariable final String crop,
		@RequestBody final ObservationUnitSearchRequestDTO requestDTO) {

		final PagedResult<ObservationUnitDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(requestDTO.getPage(), requestDTO.getPageSize(), new SearchSpec<ObservationUnitDto>() {

				@Override
				public long getCount() {
					return PhenotypeSearchResource.this.observationUnitService.countObservationUnits(requestDTO);
				}

				@Override
				public List<ObservationUnitDto> getResults(final PagedResult<ObservationUnitDto> pagedResult) {
					return PhenotypeSearchResource.this.observationUnitService
						.searchObservationUnits(pagedResult.getPageSize(), pagedResult.getPageNumber(), requestDTO);
				}
			});

		final List<ObservationUnitDto> observationUnitDtos = resultPage.getPageResults();

		final Result<ObservationUnitDto> results = new Result<ObservationUnitDto>().withData(observationUnitDtos);
		final Pagination pagination = new Pagination() //
			.withPageNumber(resultPage.getPageNumber()) //
			.withPageSize(resultPage.getPageSize()) //
			.withTotalCount(resultPage.getTotalResults()) //
			.withTotalPages(resultPage.getTotalPages());
		final Metadata metadata = new Metadata().withPagination(pagination);

		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);
	}

}
