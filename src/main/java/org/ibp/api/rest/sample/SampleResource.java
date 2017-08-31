package org.ibp.api.rest.sample;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Sample Services")
@Controller
@RequestMapping("/sample")
public class SampleResource {

	@Autowired
	public SampleService sampleService;

	@ApiOperation(value = "Get samples for a plot", notes = "Get samples for a plot")
	@RequestMapping(value = "/{crop}/samples", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<SampleDTO>> listSamples(@PathVariable final String crop,
		@RequestParam(required = true) @ApiParam(value = "The plotId to which the samples belong") final String plotId) {
		List<SampleDTO> samples = new ArrayList<>();

		List<org.generationcp.middleware.domain.sample.SampleDTO> dtos = this.sampleService.getSamples(plotId);
		if (!dtos.isEmpty()) {
			ModelMapper mapper = SampleMapper.getInstance();
			for (org.generationcp.middleware.domain.sample.SampleDTO dto : dtos) {
				SampleDTO sample = mapper.map(dto, SampleDTO.class);
				samples.add(sample);
			}
		}

		return new ResponseEntity<>(samples, HttpStatus.OK);
	}
}
