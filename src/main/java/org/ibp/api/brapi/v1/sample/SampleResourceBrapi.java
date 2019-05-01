package org.ibp.api.brapi.v1.sample;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.service.api.sample.SampleSearchRequestDto;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.sample.SampleObservationDto;
import org.ibp.api.domain.sample.SampleObservationMapper;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Api(value = "BrAPI Sample Services")
@Controller
public class SampleResourceBrapi {

	@Autowired
	private SampleService sampleService;

	@ApiOperation(value = "Get a sample by sampleId", notes = "Get a sample by sampleId")
	@RequestMapping(value = "/{crop}/brapi/v1/samples/{sampleId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<SampleObservationDto>> getSampleBySampleId(@PathVariable final String crop, final @PathVariable String sampleId) {
		final SampleDetailsDTO sampleDetailsDTO = this.sampleService.getSampleObservation(sampleId);
		if (StringUtils.isBlank(sampleDetailsDTO.getSampleBusinessKey())) {
			return new ResponseEntity<>(new SingleEntityResponse<SampleObservationDto>().withMessage("no samples found"), HttpStatus.NOT_FOUND);

		}
		ModelMapper mapper = SampleObservationMapper.getInstance();
		SampleObservationDto sampleObservationDto = mapper.map(sampleDetailsDTO, SampleObservationDto.class);
		final int resultNumber = 1;
		Pagination pagination =
			new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		final SingleEntityResponse<SampleObservationDto> singleSampleObservationResponse = new SingleEntityResponse<>(sampleObservationDto);
		singleSampleObservationResponse.getMetadata().withPagination(pagination);

		return new ResponseEntity<>(singleSampleObservationResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Samples search", notes = "Returns a list of samples")
	@RequestMapping(value = "/{crop}/brapi/v1/samples-search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<EntityListResponse<SampleObservationDto>> searchSamples(@PathVariable final String crop,
		@RequestBody final SampleSearchRequestDto requestDTO) {

		final PagedResult<SampleDetailsDTO> resultPage = new PaginatedSearch()
			.executeBrapiSearch(requestDTO.getPage(), requestDTO.getPageSize(),
				new SearchSpec<SampleDetailsDTO>() {

					@Override
					public long getCount() {
						return SampleResourceBrapi.this.sampleService.countSearchSamples(requestDTO);
					}

					@Override
					public List<SampleDetailsDTO> getResults(final PagedResult<SampleDetailsDTO> pagedResult) {
						return SampleResourceBrapi.this.sampleService.searchSamples(requestDTO);
					}
				});

		List<SampleObservationDto> sampleObservationDtos = new ArrayList<>();

		if (resultPage.getPageResults() != null) {
			final ModelMapper mapper = SampleObservationMapper.getInstance();
			for (final SampleDetailsDTO sampleDetailsDTO : resultPage.getPageResults()) {
				final SampleObservationDto sampleObservationDto = mapper.map(sampleDetailsDTO, SampleObservationDto.class);
				sampleObservationDtos.add(sampleObservationDto);
			}
		}

		final Result<SampleObservationDto> results = new Result<SampleObservationDto>().withData(sampleObservationDtos);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<SampleObservationDto> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

}
