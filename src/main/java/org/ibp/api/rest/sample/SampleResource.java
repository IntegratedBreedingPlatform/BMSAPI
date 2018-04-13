package org.ibp.api.rest.sample;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.phenotype.PhenotypeSearchSummariesDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "Sample Services")
@Controller
@RequestMapping("/sample")
public class SampleResource {

	@Autowired
	public SampleService sampleService;

	@ApiOperation(value = "Get samples", notes = "Get samples")
	@RequestMapping(value = "/{crop}/samples", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<SampleDTO>> filter(@PathVariable final String crop,
        @RequestParam(required = false) @ApiParam(value = "The plot to which the samples belong") final String plotId,
        @RequestParam(required = false) @ApiParam(value = "The list to which the samples belong") final Integer listId,
		// TODO use in swagger
		final Pageable pageable) {

		List<SampleDTO> samples = this.sampleService.filter(plotId, listId, pageable);

		return new ResponseEntity<>(samples, HttpStatus.OK);
	}
}
