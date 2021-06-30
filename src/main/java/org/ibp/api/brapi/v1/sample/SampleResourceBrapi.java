package org.ibp.api.brapi.v1.sample;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.sample.SampleObservationDto;
import org.ibp.api.domain.sample.SampleObservationMapper;
import org.ibp.api.domain.sample.SampleSummaryDto;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI Sample Services")
@Controller
public class SampleResourceBrapi {

	@Autowired
	private SampleService sampleService;


	@ApiOperation(value = "Get a sample by sampleDbId", notes = "Get a sample by sampleDbId")
	@RequestMapping(value = "/{crop}/brapi/v1/samples", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<SampleObservationDto>> getSamples(
			@PathVariable final String crop,
			@RequestParam(required = false) final String sampleDbId,
			@RequestParam(required = false) final String observationUnitDbId,
			@RequestParam(required = false) final String plateDbId,
			@RequestParam(required = false) final String germplasmDbId,
			@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
			@RequestParam(value = "page",
					required = false) final Integer currentPage,
			@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
			@RequestParam(value = "pageSize",
					required = false) final Integer pageSize
	) {
		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PagedResult<SampleDTO> resultPage = new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<SampleDTO>() {

					@Override
					public long getCount() {
						return SampleResourceBrapi.this.sampleService.countFilter(observationUnitDbId, null);
					}

					@Override
					public List<SampleDTO> getResults(final PagedResult<SampleDTO> pagedResult) {
						return SampleResourceBrapi.this.sampleService.filter(observationUnitDbId, null, new PageRequest(finalPageNumber, finalPageSize));
					}
				});
		final ModelMapper modelMapper = SampleObservationMapper.getInstance();
		final List<SampleObservationDto> sampleObservationDtos = new ArrayList<>();
		for (final SampleDTO sampleDTO : resultPage.getPageResults()) {
			final SampleObservationDto dto = modelMapper.map(sampleDTO, SampleObservationDto.class);
			dto.setSampleDbId(sampleDTO.getSampleBusinessKey());
			sampleObservationDtos.add(dto);
		}

		final Result<SampleObservationDto> result = new Result<SampleObservationDto>().withData(sampleObservationDtos);

		Pagination pagination =
				new Pagination().withPageNumber(1).withPageSize(resultPage.getPageSize())
						.withTotalCount(resultPage.getTotalResults())
						.withTotalPages(resultPage.getTotalPages());

		Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<SampleObservationDto> entityListResponse = new EntityListResponse<>(metadata, result);
		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get a sample by sampleDbId", notes = "Get a sample by sampleDbId")
	@RequestMapping(value = "/{crop}/brapi/v1/samples/{sampleDbId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SampleSummaryDto> getSampleBySampleId(@PathVariable final String crop, final @PathVariable String sampleDbId) {
		final SampleDetailsDTO sampleDetailsDTO = this.sampleService.getSampleObservation(sampleDbId);
		if (StringUtils.isBlank(sampleDetailsDTO.getSampleBusinessKey())) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message",  "not found sample"));
			final Metadata metadata = new Metadata(null, status);
			final SampleSummaryDto sampleSummaryDto = new SampleSummaryDto().setMetadata(metadata);
			return new ResponseEntity<>(sampleSummaryDto, HttpStatus.NOT_FOUND);
		}
		ModelMapper mapper = SampleObservationMapper.getInstance();
		SampleObservationDto sampleObservationDto = mapper.map(sampleDetailsDTO, SampleObservationDto.class);
		final int resultNumber = 1;
		Pagination pagination =
			new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		Metadata metadata = new Metadata().withPagination(pagination);
		SampleSummaryDto sampleSummaryDto = new SampleSummaryDto().setMetadata(metadata).setResult(sampleObservationDto);
		return new ResponseEntity<>(sampleSummaryDto, HttpStatus.OK);
	}
}
