package org.ibp.api.brapi.v1.sample;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.SampleServiceBrapi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

@Api(value = "BrAPI Sample Services")
@Controller
public class SampleResourceBrapi {

	@Autowired
	private SampleServiceBrapi sampleServiceBrapi;

	@ApiOperation(value = "Get a sample by sampleDbId", notes = "Get a sample by sampleDbId")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'MS_OBSERVATIONS', 'MS_VIEW_OBSERVATIONS', 'MS_SAMPLE_LISTS', "
		+ "'MS_IMPORT_GENOTYPES_OPTIONS', 'MS_IMPORT_GENOTYPES_FROM_GIGWA', 'MS_IMPORT_GENOTYPES_FROM_FILE', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/brapi/v1/samples/{sampleDbId}", method = RequestMethod.GET)
	@JsonView(BrapiView.BrapiV1_3.class)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<SampleObservationDto>> getSampleBySampleId(@PathVariable final String crop, final @PathVariable String sampleDbId) {
		final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO();
		requestDTO.setSampleDbIds(Collections.singletonList(sampleDbId));
		final List<SampleObservationDto> sampleObservationDtos = this.sampleServiceBrapi.getSampleObservations(requestDTO, null);

		if (!CollectionUtils.isEmpty(sampleObservationDtos)) {
			return new ResponseEntity<>(new SingleEntityResponse<>(sampleObservationDtos.get(0)), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new SingleEntityResponse<SampleObservationDto>().withMessage("not found sample"), HttpStatus.NOT_FOUND);
		}
	}
}
