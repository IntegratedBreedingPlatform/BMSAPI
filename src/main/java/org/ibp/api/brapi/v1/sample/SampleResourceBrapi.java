package org.ibp.api.brapi.v1.sample;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.domain.sample.SampleObservationDto;
import org.ibp.api.domain.sample.SampleSummaryDto;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "BrAPI Sample Services")
@Controller
public class SampleResourceBrapi {

	@Autowired private SampleService sampleService;

	@ApiOperation(value = "Get a sample by sampleId", notes = "Get a sample by sampleId")
	@RequestMapping(value = "/{crop}/brapi/v1/samples/{sampleId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SampleSummaryDto> sample(@PathVariable final String crop, final @PathVariable String sampleId) {
		SampleObservationDto sampleObservationDto = sampleService.getSampleObservation(sampleId);

		int resultNumber = (sampleObservationDto == null) ? 0 : 1;
		Pagination pagination =
			new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		Metadata metadata = new Metadata().withPagination(pagination);
		SampleSummaryDto sampleSummaryDto = new SampleSummaryDto().setMetadata(metadata).setResult(sampleObservationDto);
		return new ResponseEntity<>(sampleSummaryDto, HttpStatus.OK);
	}
}
