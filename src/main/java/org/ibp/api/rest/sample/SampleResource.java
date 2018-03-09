package org.ibp.api.rest.sample;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
        @RequestParam(required = false) @ApiParam(value = "The list to which the samples belong") final Integer listId) {

		List<SampleDTO> samples = this.sampleService.filter(plotId, listId);

		return new ResponseEntity<>(samples, HttpStatus.OK);
	}
}
