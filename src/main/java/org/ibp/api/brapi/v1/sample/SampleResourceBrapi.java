package org.ibp.api.brapi.v1.sample;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.domain.sample.SampleObservationDto;
import org.ibp.api.domain.sample.SampleObservationMapper;
import org.ibp.api.domain.sample.SampleSummaryDto;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI Sample Services")
@Controller
public class SampleResourceBrapi {

	@Autowired
	private SampleService sampleService;

	@ApiOperation(value = "Get a sample by sampleId", notes = "Get a sample by sampleId")
	@RequestMapping(value = "/{crop}/brapi/v1/samples/{sampleId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SampleSummaryDto> getSampleBySampleId(@PathVariable final String crop, final @PathVariable String sampleId) {
		final SampleDetailsDTO sampleDetailsDTO = this.sampleService.getSampleObservation(sampleId);
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
